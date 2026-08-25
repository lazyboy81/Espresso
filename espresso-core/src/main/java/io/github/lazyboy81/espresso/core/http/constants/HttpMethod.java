package io.github.lazyboy81.espresso.core.http.constants;

import java.util.Arrays;
import java.util.List;

public enum HttpMethod {

    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE");

    public static final List<HttpMethod> methods;

    private final String val;

    HttpMethod(String val) {
        this.val = val;
    }

    public String asString() {
        return val;
    }

    public static HttpMethod fromValue(String method) {
        for (HttpMethod value : values()) {
            if (value.val.equalsIgnoreCase(method)) {
                return value;
            }
        }

        return null;
    }

    static {
        methods = Arrays.asList(values());
    }
}
