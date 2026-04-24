package com.github.sinakarimi81.espresso.http;

import java.util.List;
import java.util.Map;

public record FormValues(Map<String, List<String>> values) {

    public List<String> get(String key) {
        return values.getOrDefault(key, List.of());
    }

    public Map<String, List<String>> getAll() {
        return Map.copyOf(values);
    }

}
