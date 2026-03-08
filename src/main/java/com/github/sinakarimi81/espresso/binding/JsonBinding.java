package com.github.sinakarimi81.espresso.binding;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonBinding {

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> T bind(String payload, Class<T> tClass) {
        try {
            return mapper.readValue(payload, tClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to process payload as json", e);
        }
    }

}
