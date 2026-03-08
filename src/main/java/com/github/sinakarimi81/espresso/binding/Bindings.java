package com.github.sinakarimi81.espresso.binding;

public class Bindings {

    private static final JsonBinding json = new JsonBinding();

    private Bindings() {
    }

    public static JsonBinding json() {
        return json;
    }

}
