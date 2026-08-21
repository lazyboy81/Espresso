package io.github.lazyboy81.espresso.core.routing;

import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.http.constants.HttpMethod;
import io.github.lazyboy81.espresso.core.middleware.Middleware;

import java.util.ArrayList;
import java.util.List;

public final class RouteGroup {

    private final String prefix;
    private final RouteRegistry routeRegistry;
    private final List<Middleware> groupMiddlewares;

    public RouteGroup(String prefix, RouteRegistry routeRegistry, List<Middleware> initMiddlewares) {
        this.prefix = prefix;
        this.routeRegistry = routeRegistry;
        this.groupMiddlewares = new ArrayList<>(initMiddlewares);
    }

    public void options(String path, Handler handler) {
        routeRegistry.addRoute(HttpMethod.OPTIONS, createActualPath(path), handler, groupMiddlewares);
    }

    public void head(String path, Handler handler) {
        routeRegistry.addRoute(HttpMethod.HEAD, createActualPath(path), handler, groupMiddlewares);
    }

    public void get(String path, Handler handler) {
        routeRegistry.addRoute(HttpMethod.GET, createActualPath(path), handler, groupMiddlewares);
    }

    public void post(String path, Handler handler) {
        routeRegistry.addRoute(HttpMethod.POST, createActualPath(path), handler, groupMiddlewares);
    }

    public void put(String path, Handler handler) {
        routeRegistry.addRoute(HttpMethod.PUT, createActualPath(path), handler, groupMiddlewares);
    }

    public void patch(String path, Handler handler) {
        routeRegistry.addRoute(HttpMethod.PATCH, createActualPath(path), handler, groupMiddlewares);
    }

    public void trace(String path, Handler handler) {
        routeRegistry.addRoute(HttpMethod.TRACE, createActualPath(path), handler, groupMiddlewares);
    }

    public void delete(String path, Handler handler) {
        routeRegistry.addRoute(HttpMethod.DELETE, createActualPath(path), handler, groupMiddlewares);
    }

    public void any(String path, Handler handler) {
        String actualPath = createActualPath(path);
        for (HttpMethod method : HttpMethod.methods) {
            routeRegistry.addRoute(method, actualPath, handler, groupMiddlewares);
        }
    }

    public RouteGroup group(String root) {
        if (root == null) {
            throw new IllegalArgumentException("root cannot be null");
        }

        return new RouteGroup(createActualPath(root), this.routeRegistry, groupMiddlewares);
    }

    public void use(Middleware middleware) {
        groupMiddlewares.add(middleware);
    }

    private String createActualPath(String inputPath) {
        if (!inputPath.startsWith("/")) {
            return prefix.concat("/").concat(inputPath);
        }

        if (prefix.equals("/")) {
            return inputPath;
        }

        return prefix.concat(inputPath);
    }
}
