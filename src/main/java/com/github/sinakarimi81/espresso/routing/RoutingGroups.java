package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpMethod;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutingGroups {
    private final Map<String, Handler> staticGroups;
    private final Map<String, RoutingGroup> dynamicGroups;

    /**
     * if concurrency becomes an issue we can change this to ConcurrentHashMap {@link java.util.concurrent.ConcurrentHashMap}
     */
    public RoutingGroups() {
        staticGroups = new HashMap<>();
        dynamicGroups = new HashMap<>();
    }

    public void addRoute(String method, String path, Handler handler) {
        if (HttpMethod.doesNotContain(method)) {
            throw new IllegalArgumentException(String.format("method %s is not supported in espresso", method));
        }

        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.contains("*") || path.contains(":")) {
            staticGroups.put(getStaticGroupKey(method, path), handler);
            return;
        }

        var root = dynamicGroups.get(method);
        if (root == null) {
            root = new RoutingGroup(method, new PathNode("/", "/", null, new ArrayList<>(), null));
            dynamicGroups.put(method, root);
        }

        root.addRoute(path, handler);
    }

    public Handler getHandlerForPath(String method, String path) {

        String staticGroupKey = getStaticGroupKey(method, path);
        if (staticGroups.containsKey(staticGroupKey)) {
            return staticGroups.get(staticGroupKey);
        }

        RoutingGroup routingGroup = dynamicGroups.get(method);
        if (routingGroup == null) {
            return context -> context.response().json(HttpStatus.NOT_FOUND, Map.of(
                    "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                    "status", HttpStatus.NOT_FOUND.code(),
                    "error", HttpStatus.NOT_FOUND.description(),
                    "path", String.format("%s %s", method, path)
            ));
        }

        return routingGroup.getHandlerForPath(path);
    }

    public RoutingGroup getGroup(String name) {
        return dynamicGroups.get(name);
    }

    private String getStaticGroupKey(String method, String path) {
        return method + " " + path;
    }

}
