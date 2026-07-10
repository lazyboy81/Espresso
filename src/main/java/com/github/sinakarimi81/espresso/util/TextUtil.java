package com.github.sinakarimi81.espresso.util;

public class TextUtil {

    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String RESET = "\u001B[0m";

    public static String yellow(String input) {
        return YELLOW.concat(input).concat(RESET);
    }

    public static String cyan(String input) {
        return CYAN.concat(input).concat(RESET);
    }

    public static String green(String input) {
        return GREEN.concat(input).concat(RESET);
    }

    public static String magenta(String input) {
        return MAGENTA.concat(input).concat(RESET);
    }

}
