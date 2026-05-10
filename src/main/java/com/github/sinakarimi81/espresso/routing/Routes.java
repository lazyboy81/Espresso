package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.StringUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Routes implements Router {

    private static final List<String> methods = List.of(
            HttpMethod.OPTIONS.asString(), HttpMethod.HEAD.asString(), HttpMethod.GET.asString(),
            HttpMethod.POST.asString(), HttpMethod.PUT.asString(), HttpMethod.DELETE.asString()
    );

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
        addRoute(HttpMethod.OPTIONS.asString(), path, handler);
    }

    @Override
    public void head(String path, Handler handler) {
        addRoute(HttpMethod.HEAD.asString(), path, handler);
    }

    @Override
    public void get(String path, Handler handler) {
        addRoute(HttpMethod.GET.asString(), path, handler);
    }

    @Override
    public void post(String path, Handler handler) {
        addRoute(HttpMethod.POST.asString(), path, handler);
    }

    @Override
    public void put(String path, Handler handler) {
        addRoute(HttpMethod.PUT.asString(), path, handler);
    }

    @Override
    public void delete(String path, Handler handler) {
        addRoute(HttpMethod.DELETE.asString(), path, handler);
    }

    @Override
    public void any(String path, Handler handler) {
        for (String method : methods) {
            addRoute(method, path, handler);
        }
    }

    @Override
    public Router group(String root) {
        StringUtils.validUrlInput(root);
        return new GroupedRoutes(this, root);
    }

    private void addRoute(String method, String path, Handler handler) {
        if (!methods.contains(method)) {
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
            throw new PathNotFoundException(String.format("no route was found for method: %s path: %s", method, path));
        }

        return routeContainer.getHandlerForPath(path, pathVars);
    }

    private String getStaticGroupKey(String method, String path) {
        return method + " " + path;
    }

}
