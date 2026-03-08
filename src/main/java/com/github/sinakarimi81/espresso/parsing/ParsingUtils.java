package com.github.sinakarimi81.espresso.parsing;

import com.github.sinakarimi81.espresso.exception.VersionNotSupportedException;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.util.Tuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

public class ParsingUtils {

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

    public static Headers createHeaders(BufferedReader reader) {
        try {
            Headers result = new Headers();

            String read;
            while (!(read = reader.readLine()).isBlank()) {
                String[] headerKeyValue = read.split(": ");
                result.addHeader(headerKeyValue[0], headerKeyValue[1]);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("failed to parse request headers", e);
        }
    }

    public static String getPayload(BufferedReader reader) {
        try {
            var result = new StringBuilder();

            String read;
            while ((read = reader.readLine()) != null) {
                result.append(read).append("\r\n");
            }

            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("failed to parse request headers", e);
        }
    }

}
