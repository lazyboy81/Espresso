package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.MethodConstants;

public class RouteDefinition {

    private static RouteDefinition INSTANCE;

    public static RouteDefinition getInstance() {
        if (INSTANCE == null) {
            synchronized (RouteDefinition.class) {
                if (INSTANCE == null) {
                    RoutingGroups routingGroups = RoutingGroups.getInstance();
                    INSTANCE = new RouteDefinition(routingGroups);
                }
            }
        }

        return INSTANCE;
    }

    private final RoutingGroups groups;

    private RouteDefinition(RoutingGroups routingGroups) {
        groups = routingGroups;
    }

    public void get(String path, Handler handler) {
        groups.addRoute(MethodConstants.GET_METHOD, path, handler);
    }

    public void post(String path, Handler handler) {
        groups.addRoute(MethodConstants.POST_METHOD, path, handler);
    }

    public void put(String path, Handler handler) {
        groups.addRoute(MethodConstants.PUT_METHOD, path, handler);
    }

    public void delete(String path, Handler handler) {
        groups.addRoute(MethodConstants.DELETE_METHOD, path, handler);
    }


}
