package io.github.lazyboy81.espresso.jetty;

import io.github.lazyboy81.espresso.core.engine.ResponseChannel;
import io.github.lazyboy81.espresso.core.http.Headers;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JettyResponseChannel implements ResponseChannel {

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private final Response response;
    private final Callback callback;

    @Override
    public int status() {
        return response.getStatus();
    }

    @Override
    public void status(int status) {
        response.setStatus(status);
    }

    @Override
    public void setHeader(String name, String value) {
        response.getHeaders().put(name, value);
    }

    @Override
    public void removeHeader(String name) {
        response.getHeaders().remove(name);
    }

    @Override
    public Headers getHeaders() {
        Map<String, String> collect = response.getHeaders()
                .get()
                .stream()
                .map(f -> Map.entry(f.getName(), f.getValue()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        return new Headers(collect);
    }

    @Override
    public void write(byte[] body) {
        try {
            ByteBuffer src = ByteBuffer.wrap(body);
            response.write(true, src, callback);
            captured.writeBytes(body);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Override
    public boolean committed() {
        return response.isCommitted();
    }

    @Override
    public void fail(Throwable failure) {
        Response.writeError(response.getRequest(), response, callback, failure);
    }

    @Override
    public byte[] capturedPayload() {
        return captured.toByteArray();
    }

}
