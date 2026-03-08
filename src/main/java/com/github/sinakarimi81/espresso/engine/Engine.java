package com.github.sinakarimi81.espresso.engine;

import com.github.sinakarimi81.espresso.context.Context;
import com.github.sinakarimi81.espresso.context.Request;
import com.github.sinakarimi81.espresso.context.Response;
import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.parsing.ParsingUtils;
import com.github.sinakarimi81.espresso.routing.RoutingGroup;
import com.github.sinakarimi81.espresso.routing.RoutingGroups;
import com.github.sinakarimi81.espresso.util.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

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

    public Handler getHandlerForEndpoint(BufferedReader reader) throws IOException {
        String url = reader.readLine();
        reader.readLine(); // to get pass the empty line
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

    public Context createContext(Headers headers, String payload, OutputStream outputStream) {
        return new Context(new Request(headers, payload), new Response(outputStream));
    }

}
