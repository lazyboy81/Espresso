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
        if (containsHeader(name)) {
            List<String> strings = headers.get(name);
            strings.add(value);
            headers.put(name, strings);
        } else {
            var strings = new ArrayList<String>();
            strings.add(value);
            headers.put(name, strings);
        }

    }

    public List<String> getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, List<String>> getAll() {
        return new HashMap<>(headers);
    }

}
