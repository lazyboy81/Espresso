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
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                    accepted.register(selector, SelectionKey.OP_READ, new ConnectionContext());
                }
            } else if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                ConnectionContext ctx = (ConnectionContext) key.attachment();
                boolean ready = handleAcceptedSocketChannel(channel, ctx);

                if (ready) {
                    executorService.submit(() -> processRequest(channel, ctx));
                }
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

    public boolean handleAcceptedSocketChannel(SocketChannel channel, ConnectionContext ctx) {
        try {
            ctx.readFromChannel(channel);
            switch (ctx.getState()) {
                case PARSING_HEADER: {
                    ctx.parseUrlAndHeaders();
                    if (ctx.hasNoBody()) {
                        return true;
                    }
                    // we didn't put a break here for case when full body was read with the headers
                }
                case PARSING_BODY: {
                    ctx.readAndParseBody(channel);
                    return true;
                }
                case PROCESSING_REQUEST: {
                    // to avoid trying to read the body again and again
                    break;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("an exception occurred while handling the accepted socket", e);
            ctx.reset();
            parseAndSendErrors(e, channel);
            return false;
        }
    }

    private void processRequest(SocketChannel channel, ConnectionContext ctx) {
        try {
            var pathVariablesAndHandlerTuple = extractPathVariablesAndReturnHandler(ctx.getMethod(), ctx.getPath());
            Map<String, String> pathVariables = pathVariablesAndHandlerTuple.left();
            Handler handler = pathVariablesAndHandlerTuple.right();

            var request = new Request(
                    ctx.getHeaders(),
                    new PathVariables(pathVariables),
                    ctx.getQueryParams(),
                    ctx.getFormValues(),
                    ctx.getBody()
            );


            var response = new Response(channel, ctx.getMethod());
            var context = new Context(request, response);

            ctx.reset(); // don't need it anymore

            boolean serverClose = checkForTimeout(request.headers(), channel);
            if (serverClose) {
                context.response().header("Connection", "close");
            }

            handler.handle(context);

            checkForConnectionClosure(serverClose, request.headers(), channel, ctx);
        }  catch (Exception e) {
            log.error("an exception occurred while processing the request", e);
            ctx.reset();
            parseAndSendErrors(e, channel);
        }
    }

    private Tuple<Map<String, String>, Handler> extractPathVariablesAndReturnHandler(String method, String urlPath) {
        var pathVars = new HashMap<String, String>();

        Handler handlerForPath = groups.getHandlerForPath(method, urlPath, pathVars);

        return Tuple.of(pathVars, handlerForPath);
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

    private void checkForConnectionClosure(boolean serverClose, Headers requestHeaders, SocketChannel channel, ConnectionContext ctx) throws IOException {
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
                channel.register(selector, SelectionKey.OP_READ, ctx);
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
