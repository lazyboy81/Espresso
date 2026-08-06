package io.github.lazyboy81.espresso.core.handler;

import io.github.lazyboy81.espresso.core.binding.Bindings;
import io.github.lazyboy81.espresso.core.binding.Serialization;
import io.github.lazyboy81.espresso.core.binding.dto.TemplateData;
import io.github.lazyboy81.espresso.core.engine.ResponseChannel;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.constants.HttpHeader;
import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class Response {

    private final ResponseChannel channel;

    public Response(ResponseChannel channel) {
        this.channel = channel;
    }

    public int status() {
        return channel.status();
    }

    /**
     * @return all the value of the response headers
     */
    public Headers headers() {
        return channel.getHeaders();
    }

    public byte[] capturedPayload() {
        return channel.capturedPayload();
    }

    public void header(String name, String value) {
        channel.setHeader(name, value);
    }

    /**
     * <p>serializes the given Map as JSON into the response body.
     * It also sets the Content-Type as "application/json".</p>
     *
     * @param status  the HTTP status code of the given response see {@link HttpStatus}
     * @param payload the data that should be written to JSON body
     * @throws IllegalArgumentException when a no body status code is given
     * @implNote for status codes that have not body (i.e. 204, 205, 304) you should use {@link #json(HttpStatus)}
     */
    public void json(HttpStatus status, Object payload) {
        checkHasNoBodyStatusCode(status.value());
        serialize(status, Bindings.json(), payload);
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "application/json".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void json(HttpStatus status) {
        noContent(status, Bindings.json());
    }

    /**
     * <p>serializes the given Map as XML into the response body.
     * It also sets the Content-Type as "application/xml".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param payload the data that should be written to XML body
     * @throws IllegalArgumentException when a no body status code is given
     * @implNote for status codes that have not body (i.e. 204, 205, 304) you should use {@link #xml(HttpStatus)}
     */
    public void xml(HttpStatus status, Object payload) {
        checkHasNoBodyStatusCode(status.value());
        serialize(status, Bindings.xml(), payload);
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "application/xml".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void xml(HttpStatus status) {
        noContent(status, Bindings.xml());
    }

    /**
     * <p>serializes the given formatted string into the response body.
     * It also sets the Content-Type as "text/plain".</p>
     * <p>ideally you use {@link String#format(String, Object...)} to create the response</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     * @param payload the string that is going to be sent as a response
     * @throws IllegalArgumentException when a no body status code is given
     * @implNote for status codes that have not body (i.e. 204, 205, 304) you should use {@link #text(HttpStatus)}}
     */
    public void text(HttpStatus status, String payload) {
        checkHasNoBodyStatusCode(status.value());
        serialize(status, Bindings.text(), payload);
    }

    /**
     * <p>Creates an empty response body with the given status
     * It also sets the Content-Type as "text/plain".</p>
     *
     * @param status  the HTTP status of the given response see {@link HttpStatus}
     */
    public void text(HttpStatus status) {
        noContent(status, Bindings.text());
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
    public void html(HttpStatus status, TemplateData templateData) {
        if (templateData == null) {
            throw new IllegalArgumentException("template name cannot be null");
        }

        serialize(status, Bindings.html(), templateData);
    }

    private void checkHasNoBodyStatusCode(int statusCode) {
        if (HttpStatus.NO_CONTENT.value() == statusCode) {
            throw new IllegalArgumentException(String.format("an status code (%d) that has no body was used", statusCode));
        }
    }

    private void noContent(HttpStatus status, Serialization serialization) {
        channel.status(status.value());
        channel.setHeader(HttpHeader.CONTENT_TYPE.value(), serialization.contentTypeValue());
        channel.write(new byte[0]);
    }

    private void serialize(HttpStatus status, Serialization serialization, Object payload) {
        channel.status(status.value());
        channel.setHeader(HttpHeader.CONTENT_TYPE.value(), serialization.contentTypeValue());

        String output = serialization.serialize(payload);
        channel.write(output.getBytes(StandardCharsets.UTF_8));
    }

}
