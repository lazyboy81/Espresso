package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.handler.Handler;

import java.io.IOException;

public class Espresso {

    private static Espresso INSTANCE = null;

    private static final int DEFAULT_PORT = 8080;
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
        engine = Engine.getInstance(port);
    }

    public void start() {
        try {
            engine.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void options(String path, Handler handler) {
        engine.options(path, handler);
    }

    public void head(String path, Handler handler) {
        engine.head(path, handler);
    }

    public void get(String path, Handler handler) {
        engine.get(path, handler);
    }

    public void post(String path, Handler handler) {
        engine.post(path, handler);
    }

    public void put(String path, Handler handler) {
        engine.put(path, handler);
    }

    public void delete(String path, Handler handler) {
        engine.delete(path, handler);
    }

    public void any(String path, Handler handler) {
        engine.any(path, handler);
    }

}
