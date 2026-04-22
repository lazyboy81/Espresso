package com.github.sinakarimi81.espresso.binding.impl;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sinakarimi81.espresso.binding.Binding;

public class XmlBinding extends Binding {

    private static final String CONTENT_TYPE = "application/xml; charset=utf-8";

    private final XmlMapper mapper = new XmlMapper();

    @Override
    public <T> T bind(String payload, Class<T> tClass) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        try {
            return mapper.readValue(payload, tClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to process payload as xml", e);
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
            throw new RuntimeException("failed to write input object as xml", e);
        }
    }

    @Override
    public String contentTypeValue() {
        return CONTENT_TYPE;
    }

}
