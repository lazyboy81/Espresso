package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpMethods;

public class RouteDefinition {

    private final MethodTrees trees;

    public RouteDefinition(MethodTrees methodTrees) {
        trees = methodTrees;
    }

    public void get(String path, Handler handler) {
        trees.addRoute(HttpMethods.GET, path, handler);
    }

    public void post(String path, Handler handler) {
        trees.addRoute(HttpMethods.POST, path, handler);
    }

    public void put(String path, Handler handler) {
        trees.addRoute(HttpMethods.PUT, path, handler);
    }

    public void delete(String path, Handler handler) {
        trees.addRoute(HttpMethods.DELETE, path, handler);
    }


}
