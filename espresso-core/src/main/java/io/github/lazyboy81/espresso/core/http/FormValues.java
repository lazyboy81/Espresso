package io.github.lazyboy81.espresso.core.http;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record FormValues(Map<String, String> params) {

    public FormValues(Map<String, String> params) {
        this.params = Map.copyOf(params);
    }

    public Set<String> getAllNames() {
        return params.keySet();
    }

    public String get(String key) {
        return params.get(key);
    }

    public List<String> getValues(String key) {
        String values = this.params.get(key);
        return Arrays.stream(values.split(",")).toList();
    }

    public boolean isEmpty() {
        return params == null || params.isEmpty();
    }

    public boolean hasMultipleValues(String key) {
        return params.get(key).split(",").length > 1;
    }

}
