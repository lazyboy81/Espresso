package com.github.sinakarimi81.espresso.engine;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.context.Context;
import com.github.sinakarimi81.espresso.context.Request;
import com.github.sinakarimi81.espresso.context.Response;
import com.github.sinakarimi81.espresso.exception.VersionNotSupportedException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.*;
import com.github.sinakarimi81.espresso.routing.RoutingGroups;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.Triple;
import com.github.sinakarimi81.espresso.util.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
                    INSTANCE = new Engine(port, new RoutingGroups());
                }
            }
        }

        return INSTANCE;
    }

    private static final Map<String, Tuple<Instant, Integer>> keepAlive = new ConcurrentHashMap<>();

    private volatile boolean isRunning = true;

    private final ExecutorService executorService = Executors.newFixedThreadPool(60);
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
        while (isRunning && serverSocketChannel.isOpen() && selector.isOpen()) {
            selector.select(this::acceptOrProcessRequest, 1000);
        }
    }

    /**
     * utility method for stopping the engine loop. used in tests to shut down working threads
     *
     * @throws IOException if an exception occurs during resource closure
     */
    public void stop() throws IOException {
        isRunning = false;

        executorService.shutdownNow();

        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }

        if (selector != null && selector.isOpen()) {
            selector.close();
        }
        if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
            serverSocketChannel.close();
        }
    }

    private void acceptOrProcessRequest(SelectionKey key) {
        try {
            if (key.isAcceptable()) {
                ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
                SocketChannel accepted = serverSocket.accept();
                if (accepted != null) {
                    accepted.configureBlocking(false);
                    accepted.register(selector, SelectionKey.OP_READ);
                }
            } else if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                handleAcceptedSocketChannel(channel);
            }
        } catch (Exception e) {
            // Log the error and cancel the specific key, DO NOT throw RuntimeException
            key.cancel();
            if (key.channel() != null) {
                try {
                    key.channel().close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    public void handleAcceptedSocketChannel(SocketChannel channel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(4096); // start with 4 KB
            StringBuilder requestBuilder = new StringBuilder();

            getMessageFromChannel(channel, buffer, requestBuilder);
            if (requestBuilder.isEmpty()) {
                buffer.clear();
                channel.close();
                return;
            }

            var methodPathQueryTriple = parseUrlFromRequest(requestBuilder);
            String method = methodPathQueryTriple.left();
            String path = methodPathQueryTriple.middle();
            String query = methodPathQueryTriple.right();

            var queryParams = extractQueryParams(query);
            var parsedHeaders = parseHeaders(requestBuilder);
            validateHeaders(parsedHeaders);

            var body = parseRequestBody(channel, buffer, parsedHeaders, requestBuilder);

            var formValues = parseFromValues(body, parsedHeaders);
            if (!formValues.isEmpty()) {
                // so the form data is not seen in the payload of the request
                body = "";
            }

            var pathVariablesAndHandlerTuple = extractPathVariablesAndReturnHandler(method, path);
            Map<String, String> pathVariables = pathVariablesAndHandlerTuple.left();
            Handler handler = pathVariablesAndHandlerTuple.right();

            var request = new Request(
                    parsedHeaders,
                    new PathVariables(pathVariables),
                    new Query(queryParams),
                    new FromValues(formValues),
                    body // the remainder is the body
            );
            var response = new Response(channel, method);
            var context = new Context(request, response);

            boolean serverClose = checkForTimeout(parsedHeaders, channel);
            if (serverClose) {
                context.response().header("Connection", "close");
            }

            handler.handle(context);

            checkForConnectionClosure(serverClose, parsedHeaders, channel);
        } catch (Exception e) {
            log.error("an exception occurred while handling the accepted socket", e);
            parseAndSendErrors(e, channel);
        }
    }

    private void getMessageFromChannel(SocketChannel channel, ByteBuffer buffer, StringBuilder requestBuilder) throws IOException {
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

    private Map<String, String> extractQueryParams(String query) {
        var params = new HashMap<String, String>();

        if (query.isEmpty()) {
            return params;
        }

        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            // handles the case where the query is like key= (basically a key is present with no value)
            params.put(keyValue[0], keyValue.length != 2 && param.indexOf("=") != 0 ? "" : keyValue[1]);
        }

        return params;
    }

    private Map<String, List<String>> parseFromValues(String body, Headers parsedHeaders) {
        if (!parsedHeaders.containsHeader("Content-Type") || !parsedHeaders.getHeader("Content-Type").contains("application/x-www-form-urlencoded")) {
            return Map.of();
        }

        if (body.isBlank()) {
            return Map.of();
        }

        var params = new HashMap<String, List<String>>();
        for (String pair : body.split("&")) {
            // regex is applied once, meaning the result only has two elements -> key and value
            String[] kv = pair.split("=", 2);

            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1
                    ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    : "";

            if (value.isBlank()) continue;

            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        return params;
    }

    private Tuple<Map<String, String>, Handler> extractPathVariablesAndReturnHandler(String method, String urlPath) {
        var pathVars = new HashMap<String, String>();

        Handler handlerForPath = groups.getHandlerForPath(method, urlPath, pathVars);

        return Tuple.of(pathVars, handlerForPath);
    }

    private Triple<String, String, String> parseUrlFromRequest(StringBuilder requestBuilder) {
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

        String query = "";
        if (path.contains("?")) {
            query = path.substring(path.indexOf("?") + 1);
            path = path.substring(0, path.indexOf("?"));
        }

        requestBuilder.delete(0, requestUrlEnd + 2); // so next we have to only parse headers, 2 is \r\n length
        return Triple.of(method, path, query);
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

    private boolean checkForTimeout(Headers headers, SocketChannel channel) throws IOException {
        if (headers.containsHeader("Keep-Alive")) {
            return false;
        }

        InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
        Tuple<Instant, Integer> lastKeepAlive = keepAlive.get(remoteAddress.getHostName());
        if (lastKeepAlive == null) {
            return false;
        }

        return isRequestKeepAlivePassThreshold(lastKeepAlive.left(), lastKeepAlive.right());
    }

    private boolean isRequestKeepAlivePassThreshold(Instant lastKeepAliveRequest, Integer keepAliveThreshold) {
        return Duration.between(lastKeepAliveRequest, Instant.now()).toMillis() > keepAliveThreshold;
    }

    private String parseRequestBody(SocketChannel channel, ByteBuffer buffer,
                                    Headers headers, StringBuilder requestBuilder) throws IOException {
        //TODO: another branch for chunked encoding
        if (headers.containsHeader("Content-Length")) {
            int contentLength = Integer.parseInt(headers.getHeader("Content-Length").getFirst());

            // Account for any body bytes already read during header parsing
            String existingBody = requestBuilder.toString();
            int alreadyRead = existingBody.length();
            if (alreadyRead >= contentLength) {
                // We accidentally read the whole body (or more) while reading headers
                String body = existingBody.substring(0, contentLength);
                requestBuilder.delete(0, requestBuilder.length());
                return body;
            }

            int remaining = contentLength - alreadyRead;
            requestBuilder.setLength(0);  // Clear for the remaining read

            // Temporarily switch to blocking mode to ensure we read all expected bytes
            boolean wasBlocking = channel.isBlocking();
            channel.configureBlocking(true);
            try {
                while (remaining > 0) {
                    buffer.clear();
                    int bytesRead = channel.read(buffer);
                    if (bytesRead == -1) {
                        break; // EOF – client closed connection prematurely
                    }
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    requestBuilder.append(new String(data, StandardCharsets.UTF_8));
                    remaining -= data.length;
                }
            } finally {
                channel.configureBlocking(wasBlocking); // Restore original mode
            }

            String body = existingBody + requestBuilder.toString();
            requestBuilder.delete(0, requestBuilder.length());
            return body;
        }

        return "";
    }

    private void checkForConnectionClosure(boolean serverClose, Headers requestHeaders, SocketChannel channel) throws IOException {
        if (serverClose) {
            channel.close();
            return;
        }

        List<String> connection = requestHeaders.getHeader("Connection");
        boolean containsClose = connection != null && !connection.isEmpty() && connection.stream().anyMatch(v -> v.equalsIgnoreCase("close"));

        if (containsClose) {
            channel.close();
        } else {
            // in http/1.1 connection: keep-alive is the default behavior
            List<String> keepAliveHeader = requestHeaders.getHeader("Keep-Alive");
            int keepAliveTime = keepAliveHeader != null && !keepAliveHeader.isEmpty() ? Integer.parseInt(keepAliveHeader.getFirst()) : 60;

            InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
            keepAlive.put(remoteAddress.getHostName(), Tuple.of(Instant.now(), keepAliveTime*1000));

            // Re-register ONLY if the channel is still open
            if (selector.isOpen() && channel.isOpen()) {
                channel.register(selector, SelectionKey.OP_READ);
            }
        }
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
        String jsonify = Bindings.json().serialize(HttpMethod.GET_METHOD, status, Map.of(), payload);
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
