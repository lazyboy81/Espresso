package com.github.sinakarimi81.espresso.binding.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sinakarimi81.espresso.binding.Binding;

public class JsonBinding extends Binding {

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T bind(String payload, Class<T> tClass) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        try {
            return mapper.readValue(payload, tClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to process payload as json", e);
        }
    }

    @Override
    public String convertResponsePayload(Object payload) {
        if (payload == null) {
            return "";
        }

        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("failed to write input object as json", e);
        }
    }

    @Override
    public String contentTypeValue() {
        return CONTENT_TYPE;
    }

}
