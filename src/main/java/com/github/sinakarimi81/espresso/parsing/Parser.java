package com.github.sinakarimi81.espresso.parsing;

import com.github.sinakarimi81.espresso.exception.VersionNotSupportedException;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.util.Tuple;

import java.util.List;

public class Parser {

    private static final List<String> VERSION = List.of("HTTP/1.1");

    public static void validateHttpVersion(String url) {
        String[] split = url.split(" ");

        if (!VERSION.contains(split[2])) {
            throw new VersionNotSupportedException(String.format("given version %s is not supported by espresso yet", split[2]));
        }
    }

    public static Tuple<String, String> getMethodAndPath(String url) {
        String[] split = url.split(" ");
        return Tuple.of(split[0], split[1]);
    }

    public static Headers getHeaders(List<String> rawHeaders) {
        Headers headers = new Headers();

        for (String header : rawHeaders) {
            String[] split = header.split(": ");
            headers.addHeader(split[0], split[1]);
        }

        return headers;
    }

}
