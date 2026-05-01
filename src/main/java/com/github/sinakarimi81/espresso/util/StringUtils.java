package com.github.sinakarimi81.espresso.util;

public class StringUtils {

    public static String trimBeginSlash(String original) {
        if (original.startsWith("/")) {
            return original.substring(1);
        }

        return original;
    }

    public static void validUrlInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("the given input path cannot be null/empty");
        }

        if (!input.startsWith("/")) {
            throw new IllegalArgumentException("the given input path should start with \"/\"");
        }
    }

}
