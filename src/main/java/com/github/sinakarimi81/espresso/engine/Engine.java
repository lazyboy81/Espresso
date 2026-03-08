package com.github.sinakarimi81.espresso.engine;

import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.parsing.ParsingUtils;
import com.github.sinakarimi81.espresso.routing.RoutingGroup;
import com.github.sinakarimi81.espresso.routing.RoutingGroups;
import com.github.sinakarimi81.espresso.util.Tuple;

import java.io.BufferedReader;

public class Engine {

    private static Engine INSTANCE;

    public static Engine getInstance() {
        if (INSTANCE == null) {
            synchronized (Engine.class) {
                if (INSTANCE == null) {
                    RoutingGroups routingGroups = RoutingGroups.getInstance();
                    INSTANCE = new Engine(routingGroups);
                }
            }
        }

        return INSTANCE;
    }

    private final RoutingGroups groups;

    private Engine(RoutingGroups routingGroups) {
        groups = routingGroups;
    }

    public Handler getHandlerForEndpoint(String url) {
        ParsingUtils.validateHttpVersion(url);

        Tuple<String, String> methodAndPath = ParsingUtils.getMethodAndPath(url);
        RoutingGroup group = groups.getGroup(methodAndPath.left());

        if (group == null) {
            throw new PathNotFoundException(String.format("no mapping for given url was found: %s", url), url);
        }

        return group.getHandlerForPath(methodAndPath.right());
    }

    public Headers createHeaders(BufferedReader reader) {
        return ParsingUtils.createHeaders(reader);
    }

}
