package com.github.sinakarimi81.espresso.http;

import java.util.List;

public class MethodConstants {

    public static final String GET_METHOD = "GET";
    public static final String POST_METHOD = "POST";
    public static final String PUT_METHOD = "PUT";
    public static final String DELETE_METHOD = "DELETE";

    private static final List<String> METHODS = List.of(GET_METHOD, POST_METHOD, PUT_METHOD, DELETE_METHOD);

    public static boolean doesNotContain(String method) {
        return !METHODS.contains(method);
    }

}
