package io.github.lazyboy81.espresso.core.binding.impl;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.lazyboy81.espresso.core.binding.Binding;
import io.github.lazyboy81.espresso.core.exception.BadRequestException;
import io.github.lazyboy81.espresso.core.http.constants.MediaType;

public class XmlBinding extends Binding {

    private final XmlMapper mapper = new XmlMapper();

    @Override
    public <T> T bind(byte[] payload, Class<T> tClass) {
        if (payload == null) {
            return null;
        }

        try {
            return mapper.readValue(payload, tClass);
        } catch (Exception e) {
            throw new BadRequestException("failed to process payload as xml", e);
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
            throw new RuntimeException("failed to write input object as xml", e);
        }
    }

    @Override
    public String contentTypeValue() {
        return MediaType.TEXT_XML.value();
    }

    @Override
    public boolean canHandle(String acceptType) {
        return MediaType.TEXT_XML.value().equalsIgnoreCase(acceptType);
    }

}
