package com.github.sinakarimi81.espresso.binding.impl;

import com.github.sinakarimi81.espresso.binding.Serialization;
import org.eclipse.jetty.http.MimeTypes;

public class TextBinding implements Serialization {

    @Override
    public String serialize(Object payload) {
        return payload == null ? "" : String.valueOf(payload);
    }

    @Override
    public String contentTypeValue() {
        return MimeTypes.Type.TEXT_PLAIN_UTF_8.asString();
    }

    @Override
    public boolean canHandle(String acceptType) {
        return MimeTypes.Type.TEXT_PLAIN_UTF_8.asString().equalsIgnoreCase(acceptType) ||
                MimeTypes.Type.TEXT_PLAIN_8859_1.asString().equalsIgnoreCase(acceptType) ||
                MimeTypes.Type.TEXT_PLAIN.asString().equalsIgnoreCase(acceptType);
    }

}
