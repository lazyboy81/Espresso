package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpMethod;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Routes implements Router {
    private final Map<String, Handler> staticRoutes;
    private final Map<String, RouteContainer> dynamicRoutes;

    /**
     * if concurrency becomes an issue we can change this to ConcurrentHashMap {@link java.util.concurrent.ConcurrentHashMap}
     */
    public Routes() {
        staticRoutes = new HashMap<>();
        dynamicRoutes = new HashMap<>();
    }

    @Override
    public void options(String path, Handler handler) {
        addRoute(HttpMethod.OPTIONS_METHOD, path, handler);
    }

    @Override
    public void head(String path, Handler handler) {
        addRoute(HttpMethod.HEAD_METHOD, path, handler);
    }

    @Override
    public void get(String path, Handler handler) {
        addRoute(HttpMethod.GET_METHOD, path, handler);
    }

    @Override
    public void post(String path, Handler handler) {
        addRoute(HttpMethod.POST_METHOD, path, handler);
    }

    @Override
    public void put(String path, Handler handler) {
        addRoute(HttpMethod.PUT_METHOD, path, handler);
    }

    @Override
    public void delete(String path, Handler handler) {
        addRoute(HttpMethod.DELETE_METHOD, path, handler);
    }

    @Override
    public void any(String path, Handler handler) {
        for (String method : HttpMethod.METHODS) {
            addRoute(method, path, handler);
        }
    }

    @Override
    public Router group(String root) {
        StringUtils.validUrlInput(root);
        return new GroupedRoutes(this, root);
    }

    private void addRoute(String method, String path, Handler handler) {
        if (HttpMethod.doesNotContain(method)) {
            throw new IllegalArgumentException(String.format("method %s is not supported in espresso", method));
        }

        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        // do prevent problems down the line
        if (!path.startsWith("/")) {
            path = "/".concat(path);
        }

        // this means the client is setting a handler for the '/' path
        if (path.isEmpty()) {
            staticRoutes.put(getStaticGroupKey(method, "/"), handler);
            return;
        }

        // if path does not contain any dynamic elements then it is static
        if (!path.contains("*") && !path.contains(":")) {
            staticRoutes.put(getStaticGroupKey(method, path), handler);
            return;
        }

        var root = dynamicRoutes.get(method);
        if (root == null) {
            root = new RouteContainer(method, new PathNode("/", "/", null, new ArrayList<>(), null));
            dynamicRoutes.put(method, root);
        }

        root.addRoute(path, handler);
    }

    public Handler getHandlerForPath(String method, String path, Map<String, String> pathVars) {

        String staticGroupKey = getStaticGroupKey(method, path);
        if (staticRoutes.containsKey(staticGroupKey)) {
            return staticRoutes.get(staticGroupKey);
        }

        RouteContainer routeContainer = dynamicRoutes.get(method);
        if (routeContainer == null) {
            return context -> context.response().json(HttpStatus.NOT_FOUND, Map.of(
                    "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                    "status", HttpStatus.NOT_FOUND.code(),
                    "error", HttpStatus.NOT_FOUND.description(),
                    "path", String.format("%s %s", method, path)
            ));
        }

        return routeContainer.getHandlerForPath(path, pathVars);
    }

    private String getStaticGroupKey(String method, String path) {
        return method + " " + path;
    }

}
