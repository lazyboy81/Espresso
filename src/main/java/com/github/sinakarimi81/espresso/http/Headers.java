package com.github.sinakarimi81.espresso.http;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>An interface for headers received by server from a request</p>
 */
public class Headers {

    private final Map<String, String> headers;

    public Headers() {
        headers = new HashMap<>();
    }

    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getAll() {
        return new HashMap<>(headers);
    }

}
