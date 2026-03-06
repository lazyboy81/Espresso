package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MethodTrees {

    private final Map<String, MethodTree> trees;

    public MethodTrees() {
        trees = new HashMap<>();
    }

    public void addRoute(String method, String path, Handler handler) {
        if (HttpMethods.doesNotContain(method)) {
            throw new IllegalArgumentException(String.format("method %s is not supported in espresso", method));
        }

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException(String.format("given path does not start with '/': %s", path));
        }

        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        var root = trees.get(method);
        if (root == null) {
            add(method);
            root = get(method);
        }

        root.addRoute(path, handler);
    }

    public MethodTree get(String name) {
        return trees.get(name);
    }

    private void add(String method) {
        trees.put(method, new MethodTree(method, new PathNode("/", "/", new ArrayList<>(), null)));
    }

}
