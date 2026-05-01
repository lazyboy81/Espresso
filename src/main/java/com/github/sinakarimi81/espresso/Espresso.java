package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.engine.EspressoEngine;

import java.io.IOException;

public class Espresso implements AutoCloseable {

    private static Espresso INSTANCE = null;

    private static final int DEFAULT_PORT = 8080;
    private final EspressoEngine engine;

    public static EspressoEngine getDefault() {
        try {
            if (INSTANCE == null) {
                INSTANCE = new Espresso(DEFAULT_PORT);
            }
            return INSTANCE.engine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static EspressoEngine withPort(int port) {
        try {
            if (INSTANCE == null) {
                INSTANCE = new Espresso(port);
            }
            return INSTANCE.engine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Espresso(int port) throws IOException {
        engine = EspressoEngine.getInstance(port);
    }

    public static void run() {
        INSTANCE.start();
    }

    private void start() {
        try {
            engine.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            engine.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
