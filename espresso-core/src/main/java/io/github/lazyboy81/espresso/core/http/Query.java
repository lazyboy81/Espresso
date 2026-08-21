package io.github.lazyboy81.espresso.core.http;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record Query(Map<String, String> params) {

    public Query(Map<String, String> params) {
        this.params = Map.copyOf(params);
    }

    public static Query from(Map<String, String> params) {
        return new Query(params);
    }

    public Set<String> getAllNames() {
        return params.keySet();
    }

    public String get(String key) {
        return params.get(key);
    }

    public List<String> getValues(String key) {
        return Arrays.stream(
                this.params.get(key).split(",")
        ).toList();
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

    public boolean hasMultipleValues(String key) {
        return params.get(key).split(",").length > 1;
    }

}
