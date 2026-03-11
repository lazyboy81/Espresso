package com.github.sinakarimi81.espresso.context;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Response {

    private final SocketChannel channel;
    private final Map<String, String> responseHeaders;

    public Response(SocketChannel channel) {
        this.channel = channel;
        responseHeaders = new HashMap<>();
    }

    /**
     * manage response headers
     *
     * @param key   any string, will be set as header name
     * @param value any string, if value is empty or null, any previous mapping to the given key is removed
     */
    public void header(String key, String value) {
        if (value == null || value.isBlank()) {
            responseHeaders.remove(key);
            return;
        }

        responseHeaders.put(key, value);
    }

    /**
     * <p>serializes the given Map as JSON into the response body.
     * It also sets the Content-Type as "application/json".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param payload the data that should be written to JSON body
     */
    public void json(HttpStatus status, Map<String, Object> payload) {
        String jsonify = Bindings.json().jsonify(status, responseHeaders, payload);
        try {
            ByteBuffer src = ByteBuffer.wrap(jsonify.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to send write a response", e);
        }
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "application/json".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void json(HttpStatus status) {
        String jsonify = Bindings.json().jsonify(status, responseHeaders, Map.of());
        try {
            ByteBuffer src = ByteBuffer.wrap(jsonify.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to send write a response", e);
        }
    }

}
