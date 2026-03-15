package com.github.sinakarimi81.espresso.engine;

import com.github.sinakarimi81.espresso.context.Context;
import com.github.sinakarimi81.espresso.context.Request;
import com.github.sinakarimi81.espresso.context.Response;
import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.exception.VersionNotSupportedException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.http.HttpMethod;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import com.github.sinakarimi81.espresso.routing.RoutingGroups;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.github.sinakarimi81.espresso.http.HttpVersion.SUPPORTED_VERSIONS;

@Slf4j
public class Engine {

    private static Engine INSTANCE;

    public static Engine getInstance(int port) throws IOException {
        if (INSTANCE == null) {
            synchronized (Engine.class) {
                if (INSTANCE == null) {
                    RoutingGroups routingGroups = RoutingGroups.getInstance();
                    INSTANCE = new Engine(port, routingGroups);
                }
            }
        }

        return INSTANCE;
    }

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final RoutingGroups groups;

    private Engine(int port, RoutingGroups routingGroups) throws IOException {
        groups = routingGroups;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void options(String path, Handler handler) {
        groups.addRoute(HttpMethod.OPTIONS_METHOD, path, handler);
    }

    public void head(String path, Handler handler) {
        groups.addRoute(HttpMethod.HEAD_METHOD, path, handler);
    }

    public void get(String path, Handler handler) {
        groups.addRoute(HttpMethod.GET_METHOD, path, handler);
    }

    public void post(String path, Handler handler) {
        groups.addRoute(HttpMethod.POST_METHOD, path, handler);
    }

    public void put(String path, Handler handler) {
        groups.addRoute(HttpMethod.PUT_METHOD, path, handler);
    }

    public void delete(String path, Handler handler) {
        groups.addRoute(HttpMethod.DELETE_METHOD, path, handler);
    }

    public void start() {
        while (serverSocketChannel.isOpen() && selector.isOpen()) {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                selector.select(1000);
                for (SelectionKey selectedKey : selector.selectedKeys()) {
                    if (selectedKey.isValid() && selectedKey.isAcceptable()) {
                        ServerSocketChannel serverSocket = (ServerSocketChannel) selectedKey.channel();
                        SocketChannel accepted = serverSocket.accept();
                        if (accepted != null) {
                            accepted.configureBlocking(false);
                            accepted.register(selector, SelectionKey.OP_READ);
                        }
                    } else if (selectedKey.isValid() && selectedKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectedKey.channel();
                        executor.submit(()-> handleAcceptedSocketChannel(channel));
                    }
                }
            } catch (Exception e) {
                log.error("exception occurred", e);
            }
        }
    }

    public void stop() throws IOException {
        selector.close();
        serverSocketChannel.close();
    }

    public void handleAcceptedSocketChannel(SocketChannel channel) {
        Context context = null;
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4096); // start with 4 KB
            StringBuilder requestBuilder = new StringBuilder();

            getPathAndHeadersFromChannel(channel, buffer, requestBuilder);
            if (requestBuilder.isEmpty()) {
                buffer.clear();
                channel.close();
                return;
            }

            var methodAndPathTuple = parseUrlFromRequest(requestBuilder);
            var parsedHeaders = parseHeaders(requestBuilder);
            var body = parseRequestBody(channel, buffer, parsedHeaders, requestBuilder);

            var request = new Request(parsedHeaders, body);
            var response = new Response(channel, methodAndPathTuple.left());
            context = new Context(request, response);

            findHandlerAndPassTheContext(methodAndPathTuple.left(), methodAndPathTuple.right(), context);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            log.error("an exception occurred while handling the accepted socket", e);
            parseAndSendErrors(e, context.response());
        }
    }

    private void getPathAndHeadersFromChannel(SocketChannel channel, ByteBuffer buffer, StringBuilder requestBuilder) throws IOException {
        while (channel.read(buffer) > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            requestBuilder.append(new String(data, StandardCharsets.UTF_8));
            buffer.clear();

            // check if we've read the end of headers
            int headerEnd = requestBuilder.indexOf("\r\n\r\n");
            if (headerEnd != -1) {
                break; // we have all headers
            }
        }
    }

    private Tuple<String, String> parseUrlFromRequest(StringBuilder requestBuilder) {
        int requestUrlEnd = requestBuilder.indexOf("\r\n");
        String requestLine = requestBuilder.substring(0, requestUrlEnd);
        String[] requestLineParts = requestLine.split(" ");
        String method = requestLineParts[0];
        String path = requestLineParts[1];
        String version = requestLineParts[2];

        if (!SUPPORTED_VERSIONS.contains(version)) {
            throw new VersionNotSupportedException(String.format("version %s is not supported by Espresso", version));
        }

        requestBuilder.delete(0, requestUrlEnd + 2); // so next we have to only parse headers, 2 is \r\n length
        return Tuple.of(method, path);
    }

    private Headers parseHeaders(StringBuilder requestBuilder) {
        Headers result = new Headers();

        int headerEnd = requestBuilder.indexOf("\r\n\r\n");
        String headerPart = requestBuilder.substring(0, headerEnd);

        for (String header : headerPart.split("\r\n")) {
            String[] keyValue = header.split(": ");
            result.addHeader(keyValue[0], keyValue[1]);
        }

        requestBuilder.delete(0, headerEnd + 4); // so the next time we fill request builder only body is left, 4 is \r\n\r\n length
        return result;
    }

    private String parseRequestBody(SocketChannel channel, ByteBuffer buffer,
                                    Headers headers, StringBuilder requestBuilder) throws IOException {
        //TODO: another branch for chunked encoding
        if (headers.containsHeader("Content-Length")) {
            int missing = Integer.parseInt(headers.getHeader("Content-Length"));

            // if not enough, keep reading
            while (missing > 0 && channel.read(buffer) > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                requestBuilder.append(new String(data, StandardCharsets.UTF_8));
                buffer.clear();
                missing -= data.length;
            }

            // now full body
            String body = requestBuilder.toString();
            requestBuilder.delete(0, requestBuilder.length());
            return body;
        } else {
            return "";
        }
    }

    private void findHandlerAndPassTheContext(String method, String path, Context context) {
        Handler handlerForPath = groups.getHandlerForPath(method, path);
        handlerForPath.handle(context);
    }

    private void parseAndSendErrors(Exception e, Response response) {
        switch (e) {
            case PathNotFoundException pnf -> response.json(
                    HttpStatus.NOT_FOUND, Map.of(
                            "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                            "status", HttpStatus.NOT_FOUND.code(),
                            "error", HttpStatus.NOT_FOUND.description(),
                            "message", pnf.getMessage(),
                            "path", pnf.getPath()
                    )
            );
            case VersionNotSupportedException vns -> response.json(
                    HttpStatus.BAD_REQUEST, Map.of(
                            "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                            "status", HttpStatus.BAD_REQUEST.code(),
                            "error", HttpStatus.BAD_REQUEST.description(),
                            "message", vns.getMessage()
                    )
            );
            default -> response.json(
                    HttpStatus.INTERNAL_SERVER_ERROR, Map.of(
                            "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.code(),
                            "error", HttpStatus.INTERNAL_SERVER_ERROR.description(),
                            "message", e.getMessage()
                    )
            );
        }
    }
}
