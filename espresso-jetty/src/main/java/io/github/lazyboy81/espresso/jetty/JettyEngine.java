package io.github.lazyboy81.espresso.jetty;

import io.github.lazyboy81.espresso.core.engine.ServerEngine;
import io.github.lazyboy81.espresso.core.routing.RouteResolver;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.util.concurrent.atomic.AtomicBoolean;

public class JettyEngine implements ServerEngine {

    private final JettyBlockingHandler handler;
    private final AtomicBoolean startInvoked;
    private final Server server;

    public JettyEngine(JettyOptions options, RouteResolver routeResolver) {
        this.handler = new JettyBlockingHandler(new JettyRequestProcessor(), routeResolver);
        this.startInvoked = new AtomicBoolean();
        this.server = new Server();

        // Create a ServerConnector instance on port 8080.
        ServerConnector connector = new ServerConnector(
                server,
                options.getAcceptorsOrDefault(),
                options.getSelectorsOrDefault(),
                new HttpConnectionFactory() // look this one up
        );
        connector.setPort(options.getPortOrDefault());
        server.addConnector(connector);
    }

    @Override
    public void start() {
        if (!startInvoked.compareAndSet(false, true)) {
            throw new IllegalStateException("Espresso server has already been started");
        }

        try {
            this.server.setHandler(handler);
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
