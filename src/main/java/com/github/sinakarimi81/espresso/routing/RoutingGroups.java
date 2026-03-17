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

    private static RoutingGroups INSTANCE;

    public static RoutingGroups getInstance() {
        if (INSTANCE == null) {
            synchronized (RoutingGroups.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RoutingGroups();
                }
            }
        }

        return INSTANCE;
    }

    private final Map<String, RoutingGroup> groups;

    private RoutingGroups() {
        groups = new HashMap<>();
    }

    public void addRoute(String method, String path, Handler handler) {
        if (HttpMethod.doesNotContain(method)) {
            throw new IllegalArgumentException(String.format("method %s is not supported in espresso", method));
        }

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException(String.format("given path does not start with '/': %s", path));
        }

        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        var root = groups.get(method);
        if (root == null) {
            addGroup(method);
            root = getGroup(method);
        }

        root.addRoute(path, handler);
    }

    public Handler getHandlerForPath(String method, String path) {
        RoutingGroup routingGroup = groups.get(method);
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
        return groups.get(name);
    }

    private void addGroup(String method) {
        groups.put(method, new RoutingGroup(method, new PathNode("/", "/", new ArrayList<>(), null)));
    }

}
