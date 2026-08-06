package io.github.lazyboy81.espresso.core.engine;

import io.github.lazyboy81.espresso.core.http.Headers;

public interface ResponseChannel {

    int status();

    void status(int status);

    void setHeader(String name, String value);

    void removeHeader(String name);

    Headers getHeaders();

    void write(byte[] body);

    boolean committed();

    void fail(Throwable failure);

    byte[] capturedPayload();
}
