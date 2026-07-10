package com.github.sinakarimi81.espresso.handler;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.binding.dto.TemplateData;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Response {

    private final org.eclipse.jetty.server.Response delegator;
    private final Callback callback;
    private String payload;

    public Response(org.eclipse.jetty.server.Response delegator, Callback callback) {
        this.delegator = delegator;
        this.callback = callback;
    }

    public String payload() {
        return payload;
    }

    public int status() {
        return delegator.getStatus();
    }

    /**
     * @return all the value of the response
     */
    public HttpFields headers() {
        return delegator.getHeaders().asImmutable();
    }

    /**
     * manage response value
     *
     * @param key   any string, will be set as header name
     * @param value any string, if value is empty or null, any previous mapping to the given key is removed
     */
    public void header(String key, String value) {
        delegator.getHeaders().put(key, value);
    }

    /**
     * manage response value
     *
     * @param key   any string, will be set as header name
     * @param value any string, if value is empty or null, any previous mapping to the given key is removed
     */
    public void header(HttpHeader key, String value) {
        delegator.getHeaders().put(key, value);
    }

    /**
     * <p>serializes the given Map as JSON into the response body.
     * It also sets the Content-Type as "application/json".</p>
     *
     * @param status  the HTTP status code of the given response see {@link HttpStatus}
     * @param payload the data that should be written to JSON body
     * @throws IllegalArgumentException when a no body status code is given
     * @implNote for status codes that have not body (i.e. 204, 205, 304) you should use {@link #json(HttpStatus.Code)}
     */
    public void json(HttpStatus.Code status, Object payload) {
        checkHasNoBodyStatusCode(status.getCode());

        delegator.setStatus(status.getCode());
        header(HttpHeader.CONTENT_TYPE, Bindings.json().contentTypeValue());

        try {
            String output = Bindings.json().serialize(payload);
            this.payload = String.copyValueOf(output.toCharArray());
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            delegator.write(true, src, callback);
        } catch (Exception e) {
            log.error("failed to send a response", e);
            callback.failed(e);
        }
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "application/json".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void json(HttpStatus.Code status) {
        delegator.setStatus(status.getCode());
        header(HttpHeader.CONTENT_TYPE, Bindings.json().contentTypeValue());

        try {
            ByteBuffer src = ByteBuffer.wrap(new byte[0]);
            delegator.write(true, src, callback);
        } catch (Exception e) {
            log.error("failed to send a response", e);
            callback.failed(e);
        }
    }

    /**
     * <p>serializes the given Map as XML into the response body.
     * It also sets the Content-Type as "application/xml".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param payload the data that should be written to XML body
     * @throws IllegalArgumentException when a no body status code is given
     * @implNote for status codes that have not body (i.e. 204, 205, 304) you should use {@link #xml(HttpStatus.Code)}
     */
    public void xml(HttpStatus.Code status, Object payload) {
        checkHasNoBodyStatusCode(status.getCode());

        delegator.setStatus(status.getCode());
        header(HttpHeader.CONTENT_TYPE, Bindings.xml().contentTypeValue());

        try {
            String output = Bindings.xml().serialize(payload);
            this.payload = String.copyValueOf(output.toCharArray());
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            delegator.write(true, src, callback);
        } catch (Exception e) {
            log.error("failed to send a response", e);
            callback.failed(e);
        }
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "application/xml".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void xml(HttpStatus.Code status) {
        delegator.setStatus(status.getCode());
        header(HttpHeader.CONTENT_TYPE, Bindings.xml().contentTypeValue());

        try {
            ByteBuffer src = ByteBuffer.wrap(new byte[0]);
            delegator.write(true, src, callback);
        } catch (Exception e) {
            log.error("failed to send a response", e);
            callback.failed(e);
        }
    }

    /**
     * <p>serializes the given formatted string into the response body.
     * It also sets the Content-Type as "text/plain".</p>
     * <p>ideally you use {@link String#format(String, Object...)} to create the response</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param payload the string that is going to be sent as a response
     * @throws IllegalArgumentException when a no body status code is given
     * @implNote for status codes that have not body (i.e. 204, 205, 304) you should use {@link #text(HttpStatus.Code)}}
     */
    public void text(HttpStatus.Code status, String payload) {
        checkHasNoBodyStatusCode(status.getCode());

        delegator.setStatus(status.getCode());
        header(HttpHeader.CONTENT_TYPE, Bindings.text().contentTypeValue());

        try {
            String output = Bindings.text().serialize(payload);
            this.payload = String.copyValueOf(output.toCharArray());
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            delegator.write(true, src, callback);
        } catch (Exception e) {
            log.error("failed to send a response", e);
            callback.failed(e);
        }
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "text/plain".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void text(HttpStatus.Code status) {
        delegator.setStatus(status.getCode());
        header(HttpHeader.CONTENT_TYPE, Bindings.text().contentTypeValue());

        try {
            ByteBuffer src = ByteBuffer.wrap(new byte[0]);
            delegator.write(true, src, callback);
        } catch (Exception e) {
            log.error("failed to send a response", e);
            callback.failed(e);
        }
    }

    /**
     * <p>serializes the given formatted string into the response body.
     * It also sets the Content-Type as "text/plain".</p>
     * <p>ideally you use {@link String#format(String, Object...)} to create the response</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param templateData data needed to render an HTML template see {@link TemplateData}
     * @throws IllegalArgumentException when {@code templateData} is null
     */
    public void html(HttpStatus.Code status, TemplateData templateData) {
        if (templateData == null) {
            throw new IllegalArgumentException("template name cannot be null");
        }

        delegator.setStatus(status.getCode());
        header(HttpHeader.CONTENT_TYPE, Bindings.html().contentTypeValue());

        try {
            String output = Bindings.html().serialize(templateData);
            this.payload = String.copyValueOf(output.toCharArray());
            ByteBuffer src = ByteBuffer.wrap(output.getBytes(StandardCharsets.UTF_8));
            delegator.write(true, src, callback);
        } catch (Exception e) {
            log.error("failed to send a response", e);
            callback.failed(e);
        }
    }

    private void checkHasNoBodyStatusCode(int statusCode) {
        if (HttpStatus.hasNoBody(statusCode)) {
            throw new IllegalArgumentException(String.format("an status code (%d) that has no body was used", statusCode));
        }
    }

}
