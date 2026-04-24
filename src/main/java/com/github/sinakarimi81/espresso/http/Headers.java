package com.github.sinakarimi81.espresso.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>An interface for headers received by server from a request</p>
 */
public class Headers {

    private final Map<String, List<String>> headers;

    public Headers() {
        headers = new HashMap<>();
    }

    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    public void addHeader(String name, String value) {
        headers.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
    }

    public List<String> getHeader(String name) {
        return headers.getOrDefault(name, List.of());
    }

    public boolean hasValue(String name, String value) {
        List<String> headerValues = getHeader(name);
        return headerValues.contains(value);
    }

    public Map<String, List<String>> getAll() {
        return new HashMap<>(headers);
    }

}
