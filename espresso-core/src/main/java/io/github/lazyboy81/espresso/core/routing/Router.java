package io.github.lazyboy81.espresso.core.routing;

import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.middleware.Middleware;

import java.util.LinkedHashSet;

public interface Router {

    void options(String path, Handler handler);
    void head(String path, Handler handler);
    void get(String path, Handler handler);
    void post(String path, Handler handler);
    void put(String path, Handler handler);
    void delete(String path, Handler handler);
    void any(String path, Handler handler);
    Router group(String root);
    void use(Middleware middleware);

    default Handler applyMiddlewares(Handler original, LinkedHashSet<Middleware> middlewares) {

        for (Middleware middleware : middlewares) {
            original = middleware.handle(original);
        }

        return original;
    }

}
