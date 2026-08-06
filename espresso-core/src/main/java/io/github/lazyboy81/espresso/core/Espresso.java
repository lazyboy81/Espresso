package io.github.lazyboy81.espresso.core;

import io.github.lazyboy81.espresso.core.engine.ServerEngine;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.middleware.Middleware;
import io.github.lazyboy81.espresso.core.routing.Router;
import io.github.lazyboy81.espresso.core.routing.Routes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class Espresso {

    private static final int DEFAULT_PORT = 8080;

    private final Routes routes;
    private final ServerEngine server;

    public static Espresso getDefault() {
        try {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: replace with a Options class which we will use to config the server
    public static Espresso withPort(int port) {
        try {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Espresso(int port) {

        this.routes = new Routes();
        this.server = null;

        // this is blocking
        this.server.setHandler(new org.eclipse.jetty.server.Handler.Abstract() {

            @Override
            public boolean handle(Request request, Response response, Callback callback) {
                CompletableFuture.runAsync(() -> process(request, response, callback), executorService);
                return true;
            }

        });
    }

    public Router group(String root) {
        return routes.group(root);
    }

    /**
     * global middlewares to be used with all the registered endpoints
     * @implNote middlewares are applied in the order that they are defined, so be mindful of how you apply them
     * for example:
     * <pre>{@code
     *  espresso.use(requsetIdMiddleware)
     *  espresso.use(loggerMiddleware)
     *  }</pre>
     *  means that first the requestIdMiddleware is applied and then the loggerMiddleware
     * @param middleware a middleware to be applied
     */
    public void use(Middleware middleware) {
        routes.use(middleware);
    }

    public void options(String path, Handler handler) {
        routes.options(path, handler);
    }

    public void head(String path, Handler handler) {
        routes.head(path, handler);
    }

    public void get(String path, Handler handler) {
        routes.get(path, handler);
    }

    public void post(String path, Handler handler) {
        routes.post(path, handler);
    }

    public void put(String path, Handler handler) {
        routes.put(path, handler);
    }

    public void delete(String path, Handler handler) {
        routes.delete(path, handler);
    }

    public void any(String path, Handler handler) {
        routes.any(path, handler);
    }

}
