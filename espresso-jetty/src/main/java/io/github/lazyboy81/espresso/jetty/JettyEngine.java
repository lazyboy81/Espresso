package io.github.lazyboy81.espresso.jetty;

import io.github.lazyboy81.espresso.core.engine.ServerEngine;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

import java.util.concurrent.atomic.AtomicBoolean;

public class JettyEngine implements ServerEngine {

    private final AtomicBoolean startInvoked;
    private final Server server;

    public JettyEngine(int port) {
        this.startInvoked = new AtomicBoolean();
        this.server = new Server(port);
    }

    @Override
    public void start() {
        if (!startInvoked.compareAndSet(false, true)) {
            throw new IllegalStateException("Espresso server has already been started");
        }

        try {
            this.server.setHandler(new org.eclipse.jetty.server.Handler.Abstract() {

                @Override
                public boolean handle(Request request, Response response, Callback callback) {
                    var processor = new JettyRequestProcessor(callback);
                    processor.processRequest(request);
                    processor.processResponse(response);
                    return true;
                }

            });

            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start the server", e);
        } finally {
            startInvoked.set(false);
        }
    }

    @Override
    public void stop() {
        try {
            if (server.isRunning() || server.isStarting()) {
                server.stop();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to stop the server", e);
        }
    }

    @Override
    public void join() {
        try {
            server.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Server thread was interrupted", e);
        } finally {
            startInvoked.set(false);
        }
    }

    @Override
    public int boundPort() {
        return ((ServerConnector) server.getConnectors()[0]).getPort();
    }

    @Override
    public void close() {
        stop();
    }

}
