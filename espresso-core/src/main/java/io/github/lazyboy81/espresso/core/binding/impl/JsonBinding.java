package io.github.lazyboy81.espresso.core.binding.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lazyboy81.espresso.core.binding.Binding;
import io.github.lazyboy81.espresso.core.exception.BadRequestException;
import io.github.lazyboy81.espresso.core.http.constants.MediaType;

public class JsonBinding extends Binding {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T bind(byte[] payload, Class<T> tClass) {
        if (payload == null) {
            return null;
        }

        try {
            return mapper.readValue(payload, tClass);
        } catch (Exception e) {
            throw new BadRequestException("failed to process payload as json", e);
        }
    }

    @Override
    public String serialize(Object payload) {
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
        return MediaType.APPLICATION_JSON.value();
    }

    @Override
    public boolean canHandle(String acceptType) {
        return MediaType.APPLICATION_JSON.value().equalsIgnoreCase(acceptType);
    }

}
