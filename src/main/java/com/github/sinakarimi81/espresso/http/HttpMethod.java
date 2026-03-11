package com.github.sinakarimi81.espresso.http;

import java.util.List;

public class HttpMethod {

    public static final String OPTIONS_METHOD = "OPTIONS";
    public static final String HEAD_METHOD = "HEAD";
    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    public static final String PUT_METHOD = "PUT";
    public static final String DELETE_METHOD = "DELETE";

    private static final List<String> METHODS = List.of(
            OPTIONS_METHOD, HEAD_METHOD, GET_METHOD,
            POST_METHOD, PUT_METHOD, DELETE_METHOD
    );

    public static boolean doesNotContain(String method) {
        return !METHODS.contains(method);
    }

}
