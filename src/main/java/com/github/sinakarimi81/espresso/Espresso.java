package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.management.ObjectManager;
import com.github.sinakarimi81.espresso.parsing.Parser;
import com.github.sinakarimi81.espresso.routing.RouteDefinition;
import com.github.sinakarimi81.espresso.util.Tuple;

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
        engine = ObjectManager.getInstance().engine();
        routeDefinition = ObjectManager.getInstance().routeDefinition();
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
        try (var bufferedReader = new BufferedReader(new InputStreamReader(acceptedSocket.getInputStream()));
             var bufferedWriter = new BufferedWriter(new OutputStreamWriter(acceptedSocket.getOutputStream()))) {

            String url = bufferedReader.readLine();
            Handler handlerForEndpoint = engine.getHandlerForEndpoint(url);

//            List<String> headers = new ArrayList<>();
//            String headerLine;
//            do {
//                headerLine = bufferedReader.readLine();
//                headers.add(headerLine);
//            } while (!headerLine.isBlank());
//
//            StringBuilder payload = new StringBuilder();
//            bufferedReader.lines().forEach(payload::append);
//
//            Context context = ContextManager.createContext();
        } catch (Exception e) {
            throw new RuntimeException("error in handling request/response", e);
        }
    }

}
