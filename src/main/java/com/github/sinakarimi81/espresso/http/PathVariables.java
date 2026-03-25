package com.github.sinakarimi81.espresso.http;

import java.util.Map;

public record PathVariables(Map<String, String> params) {

    public String get(String key) {
        return params.getOrDefault(key, "");
    }

}
