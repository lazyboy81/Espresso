package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.context.Context;
import com.github.sinakarimi81.espresso.engine.Engine;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Espresso {

    private static Espresso INSTANCE = null;

    private static final int DEFAULT_PORT = 8080;
    private final ServerSocket serverSocket;
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
        engine = new Engine();
    }

    public void start() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            while (true) {
                Socket acceptedSocket = serverSocket.accept();
                executor.submit(() -> new Context(engine, acceptedSocket.getInputStream(), acceptedSocket.getOutputStream()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
