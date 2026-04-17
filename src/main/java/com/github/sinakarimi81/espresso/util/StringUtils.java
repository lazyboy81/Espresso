package com.github.sinakarimi81.espresso.util;

public class StringUtils {

    public static String trimBeginSlash(String original) {
        if (original.startsWith("/")) {
            return original.substring(1);
        }

        return original;
    }

}
