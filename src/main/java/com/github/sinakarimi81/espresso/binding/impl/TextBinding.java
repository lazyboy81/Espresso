package com.github.sinakarimi81.espresso.binding.impl;

import com.github.sinakarimi81.espresso.binding.Serialization;

public class TextBinding extends Serialization {

    private static final String CONTENT_TYPE = "text/plain; charset=utf-8";

    @Override
    public String convertResponsePayload(Object payload) {
        return payload == null ? "" : String.valueOf(payload);
    }

    @Override
    public String contentTypeValue() {
        return CONTENT_TYPE;
    }
}
