package com.github.sinakarimi81.espresso.binding.impl;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sinakarimi81.espresso.binding.Binding;
import com.github.sinakarimi81.espresso.exception.BadRequestException;
import org.eclipse.jetty.http.MimeTypes;

import java.io.InputStream;

public class XmlBinding extends Binding {

    private final XmlMapper mapper = new XmlMapper();

    @Override
    public <T> T bind(InputStream payload, Class<T> tClass) {
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
        return MimeTypes.Type.TEXT_XML_UTF_8.asString();
    }

    @Override
    public boolean canHandle(String acceptType) {
        return MimeTypes.Type.TEXT_XML_UTF_8.asString().equalsIgnoreCase(acceptType) ||
                MimeTypes.Type.TEXT_XML_8859_1.asString().equalsIgnoreCase(acceptType) ||
                MimeTypes.Type.TEXT_XML.asString().equalsIgnoreCase(acceptType);
    }

}
