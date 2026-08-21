package io.github.lazyboy81.espresso.core.routing;

import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.http.PathVariables;

import java.util.Objects;

public record RouteMatch(PathVariables pathVariables,
                         Handler handler) {

    public RouteMatch {
        Objects.requireNonNull(pathVariables, "Path variables cannot be null");
        Objects.requireNonNull(handler, "handler cannot be null");
    }
}
