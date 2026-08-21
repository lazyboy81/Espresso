package io.github.lazyboy81.espresso.core.http;

import java.util.Map;

public record PathVariables(Map<String, String> params) {

    public PathVariables(Map<String, String> params) {
        this.params = Map.copyOf(params);
    }

    public boolean isEmpty() {
        return params == null || params.isEmpty();
    }

    public String get(String key) {
        return params.getOrDefault(key, "");
    }

}
