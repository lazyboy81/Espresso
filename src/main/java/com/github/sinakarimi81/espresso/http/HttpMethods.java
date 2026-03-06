package com.github.sinakarimi81.espresso.http;

import java.util.List;

public class HttpMethods {

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    private static final List<String> METHODS = List.of(GET, POST, PUT, DELETE);

    public static boolean doesNotContain(String method) {
        return !METHODS.contains(method);
    }

}
