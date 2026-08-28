package io.github.lazyboy81.espresso.core.handler;

import io.github.lazyboy81.espresso.core.binding.Serialization;
import io.github.lazyboy81.espresso.core.binding.dto.TemplateData;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;

import java.nio.charset.StandardCharsets;

public abstract class Response {

    protected byte[] serialize(Serialization serialization, Object payload) {
        String output = serialization.serialize(payload);
        return output.getBytes(StandardCharsets.UTF_8);
    }

    public abstract int status();

    public abstract Headers headers();

    public abstract void header(String name, String value);

    public abstract void json(HttpStatus status, Object payload);

    public abstract void json(HttpStatus status);

    public abstract void xml(HttpStatus status, Object payload);

    public abstract void xml(HttpStatus status);

    public abstract void text(HttpStatus status, String payload);

    public abstract void text(HttpStatus status);

    public abstract void html(HttpStatus status, TemplateData templateData);

}
