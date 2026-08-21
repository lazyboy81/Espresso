package io.github.lazyboy81.espresso.core.routing;

import java.util.Map;

public record PatternMatch(Map<String, String> pathParameters) {

    public PatternMatch {
        pathParameters = Map.copyOf(pathParameters);
    }
}
