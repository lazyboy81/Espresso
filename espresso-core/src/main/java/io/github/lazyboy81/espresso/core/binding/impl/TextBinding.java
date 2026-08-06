package io.github.lazyboy81.espresso.core.binding.impl;

import io.github.lazyboy81.espresso.core.binding.Serialization;
import io.github.lazyboy81.espresso.core.http.constants.MediaType;

public class TextBinding implements Serialization {

    @Override
    public String serialize(Object payload) {
        return payload == null ? "" : String.valueOf(payload);
    }

    @Override
    public String contentTypeValue() {
        return MediaType.TEXT_PLAIN.value();
    }

    @Override
    public boolean canHandle(String acceptType) {
        return MediaType.TEXT_PLAIN.value().equalsIgnoreCase(acceptType);
    }

}
