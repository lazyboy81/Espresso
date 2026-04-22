package com.github.sinakarimi81.espresso.context;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.binding.dto.HtmlData;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class Response {

    private final SocketChannel channel;
    private final String requestMethod;
    private final Map<String, String> responseHeaders;

    public Response(SocketChannel channel, String method) {
        this.channel = channel;
        this.requestMethod = method;
        this.responseHeaders = new HashMap<>();
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
    public void json(HttpStatus status, Object payload) {
        String jsonify = Bindings.json().serialize(requestMethod, status, responseHeaders, payload);
        try {
            ByteBuffer src = ByteBuffer.wrap(jsonify.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to send a response", e);
        }
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "application/json".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void json(HttpStatus status) {
        String jsonify = Bindings.json().serialize(requestMethod, status, responseHeaders, null);
        try {
            ByteBuffer src = ByteBuffer.wrap(jsonify.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to write a response", e);
        }
    }

    /**
     * <p>serializes the given Map as XML into the response body.
     * It also sets the Content-Type as "application/xml".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param payload the data that should be written to XML body
     */
    public void xml(HttpStatus status, Object payload) {
        String output = Bindings.xml().serialize(requestMethod, status, responseHeaders, payload);
        try {
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to send a response", e);
        }
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "application/xml".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void xml(HttpStatus status) {
        String jsonify = Bindings.xml().serialize(requestMethod, status, responseHeaders, null);
        try {
            ByteBuffer src = ByteBuffer.wrap(jsonify.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to write a response", e);
        }
    }

    /**
     * <p>serializes the given formatted string into the response body.
     * It also sets the Content-Type as "text/plain".</p>
     * <p>ideally you use {@link String#format(String, Object...)} to create the response</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param payload the string that is going to be sent as a response
     */
    public void text(HttpStatus status, String payload) {
        String output = Bindings.text().serialize(requestMethod, status, responseHeaders, payload);
        try {
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to send a response", e);
        }
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "text/plain".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void text(HttpStatus status) {
        String jsonify = Bindings.text().serialize(requestMethod, status, responseHeaders, "");
        try {
            ByteBuffer src = ByteBuffer.wrap(jsonify.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to write a response", e);
        }
    }

    /**
     * <p>serializes the given formatted string into the response body.
     * It also sets the Content-Type as "text/plain".</p>
     * <p>ideally you use {@link String#format(String, Object...)} to create the response</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param templateName name of the HTML template to serialize
     */
    public void html(HttpStatus status, String templateName) {
        String output = Bindings.html().serialize(requestMethod, status, responseHeaders, new HtmlData(templateName, Locale.getDefault(), Map.of()));
        try {
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to send a response", e);
        }
    }

    /**
     * <p>serializes the given formatted string into the response body.
     * It also sets the Content-Type as "text/plain".</p>
     * <p>ideally you use {@link String#format(String, Object...)} to create the response</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param templateName name of the HTML template to serialize
     * @param locale locale to use to serialize the HTML
     */
    public void html(HttpStatus status, String templateName, Locale locale) {
        String output = Bindings.html().serialize(requestMethod, status, responseHeaders, new HtmlData(templateName, locale, Map.of()));
        try {
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to send a response", e);
        }
    }

    /**
     * <p>serializes the given formatted string into the response body.
     * It also sets the Content-Type as "text/plain".</p>
     * <p>ideally you use {@link String#format(String, Object...)} to create the response</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param templateName name of the HTML template to serialize
     * @param locale locale to use to serialize the HTML
     * @param vars value for any placeholders defined in the template
     */
    public void html(HttpStatus status, String templateName, Locale locale, Map<String, Object> vars) {
        String output = Bindings.html().serialize(requestMethod, status, responseHeaders, new HtmlData(templateName, locale, vars));
        try {
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            channel.write(src);
        } catch (Exception e) {
            log.error("failed to send a response", e);
        }
    }

}
