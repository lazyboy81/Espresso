package io.github.lazyboy81.espresso.starter;

import io.github.lazyboy81.espresso.core.engine.ServerEngine;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.middleware.Middleware;
import io.github.lazyboy81.espresso.core.routing.RouteGroup;
import io.github.lazyboy81.espresso.core.routing.RouteRegistry;
import io.github.lazyboy81.espresso.jetty.JettyEngine;
import io.github.lazyboy81.espresso.jetty.JettyOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Espresso {

    private final RouteRegistry routeRegistry;
    private final ServerEngine server;

    public Espresso(JettyOptions options) {
        this.routeRegistry = new RouteRegistry();
        this.server = new JettyEngine(options, routeRegistry);
    }

    /**
     * global middlewares to be used with all the registered endpoints
     * @apiNote <p>middlewares should be defined before any route registration.</br>
     * Also they are applied in the order that they are defined, so be mindful of how you apply them
     * for example:
     * <pre>{@code
     *  espresso.use(requsetIdMiddleware)
     *  espresso.use(loggerMiddleware)
     *  }</pre>
     *  means that first the requestIdMiddleware is applied and then the loggerMiddleware</p>
     * @param middleware a middleware to be applied
     */
    public void use(Middleware middleware) {
        routeRegistry.use(middleware);
    }

    public RouteGroup group(String root) {
        return routeRegistry.group(root);
    }

    public void options(String path, Handler handler) {
        routeRegistry.options(path, handler);
    }

    public void head(String path, Handler handler) {
        routeRegistry.head(path, handler);
    }

    public void get(String path, Handler handler) {
        routeRegistry.get(path, handler);
    }

    public void post(String path, Handler handler) {
        routeRegistry.post(path, handler);
    }

    public void put(String path, Handler handler) {
        routeRegistry.put(path, handler);
    }

    public void delete(String path, Handler handler) {
        routeRegistry.delete(path, handler);
    }

    public void any(String path, Handler handler) {
        routeRegistry.any(path, handler);
    }

    public void start() {
        server.start();
        server.join();
    }

    public void shutdown() {
        server.stop();
    }

    public int boundPort() {
        return server.boundPort();
    }

}
