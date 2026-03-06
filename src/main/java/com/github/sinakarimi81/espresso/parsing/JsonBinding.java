package com.github.sinakarimi81.espresso.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonBinding implements Binding {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T bind(String payload, Class<T> tClass) {
        try {
            return mapper.readValue(payload, tClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to process payload as json", e);
        }
    }

}
