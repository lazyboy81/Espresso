package com.github.sinakarimi81.espresso.binding.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sinakarimi81.espresso.binding.Binding;
import com.github.sinakarimi81.espresso.exception.BadRequestException;
import org.eclipse.jetty.http.MimeTypes;

import java.io.InputStream;

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
        return MimeTypes.Type.APPLICATION_JSON_UTF_8.asString();
    }

    @Override
    public boolean canHandle(String acceptType) {
        return MimeTypes.Type.APPLICATION_JSON_UTF_8.asString().equalsIgnoreCase(acceptType) ||
                MimeTypes.Type.APPLICATION_JSON_8859_1.asString().equalsIgnoreCase(acceptType) ||
                MimeTypes.Type.APPLICATION_JSON.asString().equalsIgnoreCase(acceptType);
    }

}
