package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.context.Context;
import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.parsing.ParsingUtils;
import com.github.sinakarimi81.espresso.routing.RouteDefinition;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Espresso {

    private static Espresso INSTANCE = null;

    private static final int DEFAULT_PORT = 8080;
    private final ServerSocket serverSocket;
    private final RouteDefinition routeDefinition;
    private final Engine engine;

    public static Espresso getDefault() {
        try {
            if (INSTANCE == null) {
                INSTANCE = new Espresso(DEFAULT_PORT);
            }
            return INSTANCE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Espresso withPort(int port) {
        try {
            if (INSTANCE == null) {
                INSTANCE = new Espresso(port);
            }
            return INSTANCE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Espresso(int port) throws IOException {
        serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
        engine = Engine.getInstance();
        routeDefinition = RouteDefinition.getInstance();
    }

    public RouteDefinition routeDefinition() {
        return routeDefinition;
    }

    public void start() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            while (true) {
                Socket acceptedSocket = serverSocket.accept();
                executor.submit(() -> handleSocket(acceptedSocket));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleSocket(Socket acceptedSocket) {
        try (var reader = new BufferedReader(new InputStreamReader(acceptedSocket.getInputStream()));
             var writer = acceptedSocket.getOutputStream()) {

            Handler handlerForEndpoint = engine.getHandlerForEndpoint(reader);

            Headers headers = engine.createHeaders(reader);

            Context context = engine.createContext(headers, ParsingUtils.getPayload(reader), writer);

            handlerForEndpoint.handle(context);

        } catch (Exception e) {
            throw new RuntimeException("error in handling request/response", e);
        }
    }

}
