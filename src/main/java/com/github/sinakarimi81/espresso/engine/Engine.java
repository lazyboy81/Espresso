package com.github.sinakarimi81.espresso.engine;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.context.Context;
import com.github.sinakarimi81.espresso.context.Request;
import com.github.sinakarimi81.espresso.context.Response;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
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

    public void any(String path, Handler handler) {
        for (String method : HttpMethod.METHODS) {
            groups.addRoute(method, path, handler);
        }
    }

    public void start() throws IOException {
        while (serverSocketChannel.isOpen() && selector.isOpen()) {
            selector.select(this::acceptOrProcessRequest, 1000);
        }
    }

    private void acceptOrProcessRequest(SelectionKey key) {
        try {
            if (key.isValid() && key.isAcceptable()) {
                ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
                SocketChannel accepted = serverSocket.accept();
                if (accepted != null) {
                    accepted.configureBlocking(false);
                    accepted.register(selector, SelectionKey.OP_READ);
                }
            } else if (key.isValid() && key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                handleAcceptedSocketChannel(channel);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * utility method for stopping the engine loop. used in tests to shut down working threads
     *
     * @throws IOException if an exception occurs during resource closure
     */
    public void stop() throws IOException {
        selector.close();
        serverSocketChannel.close();
    }

    public void handleAcceptedSocketChannel(SocketChannel channel) {
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
            validateHeaders(parsedHeaders);
            var body = parseRequestBody(channel, buffer, parsedHeaders, requestBuilder);

            var request = new Request(parsedHeaders, body);
            var response = new Response(channel, methodAndPathTuple.left());
            var context = new Context(request, response);

            findHandlerAndPassTheContext(methodAndPathTuple.left(), methodAndPathTuple.right(), context);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            log.error("an exception occurred while handling the accepted socket", e);
            parseAndSendErrors(e, channel);
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
            log.error("version {} is not supported by Espresso", version);
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
            if (keyValue[1].contains(";")) {
                for (String val : keyValue[1].split(";")) {
                    result.addHeader(keyValue[0], val);
                }
            } else {
                result.addHeader(keyValue[0], keyValue[1]);
            }
        }

        requestBuilder.delete(0, headerEnd + 4); // so the next time we fill request builder only body is left, 4 is \r\n\r\n length
        return result;
    }

    private void validateHeaders(Headers headers) {
        if (headers.containsHeader("Upgrade")) {
            List<String> upgrade = headers.getHeader("Upgrade");
            if (upgrade.contains("h2c")) {
                log.error("request contains Upgrade: h2c header. http version 2 is not supported by Espresso");
                throw new VersionNotSupportedException("request contains Upgrade: h2c header, http version 2 is not supported by Espresso");
            }
        }
    }

    private String parseRequestBody(SocketChannel channel, ByteBuffer buffer,
                                    Headers headers, StringBuilder requestBuilder) throws IOException {
        //TODO: another branch for chunked encoding
        if (headers.containsHeader("Content-Length")) {
            int missing = Integer.parseInt(headers.getHeader("Content-Length").getFirst());

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

    private void findHandlerAndPassTheContext(String method, String path, Context context) throws Exception {
        Handler handlerForPath = groups.getHandlerForPath(method, path);
        handlerForPath.handle(context);
    }

    private void parseAndSendErrors(Exception e, SocketChannel channel) {
        if (Objects.requireNonNull(e) instanceof VersionNotSupportedException vns) {
            sendError(channel, vns, HttpStatus.BAD_REQUEST, Map.of(
                    "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                    "status", HttpStatus.BAD_REQUEST.code(),
                    "error", HttpStatus.BAD_REQUEST.description(),
                    "message", vns.getMessage()
            ));
        } else {
            sendError(channel, e, HttpStatus.INTERNAL_SERVER_ERROR, Map.of(
                    "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.code(),
                    "error", HttpStatus.INTERNAL_SERVER_ERROR.description(),
                    "message", e.getMessage()
            ));
        }
    }

    private void sendError(SocketChannel channel, Exception e, HttpStatus status, Map<String, Object> payload) {
        String jsonify = Bindings.json().jsonify(HttpMethod.GET_METHOD, status, Map.of(), payload);
        if (channel.isOpen()) {
            try {
                ByteBuffer src = ByteBuffer.wrap(jsonify.getBytes(StandardCharsets.UTF_8));
                channel.write(src);
            } catch (Exception ignored) {
                log.error("failed to write a response", e);
            }
        } else {
            log.error("channel was closed so logging an exception.\n {}", jsonify, e);
        }
    }
}
