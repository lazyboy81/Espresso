package io.github.lazyboy81.espresso.core.handler;

import io.github.lazyboy81.espresso.core.binding.Bindings;
import io.github.lazyboy81.espresso.core.http.FormValues;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.PathVariables;
import io.github.lazyboy81.espresso.core.http.Query;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Request {

    private final String method;
    private final String path;
    private final Headers headers;
    private final Query query;
    private final PathVariables pathVariables;
    private final FormValues formValues;
    // TODO: we can later switch to ByteBuffer and off-heap memory if needed
    private final byte[] payload;

    private Request(String method, String path, Headers headers, Query query, PathVariables pathVariables, FormValues formValues, byte[] payload) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.query = query;
        this.pathVariables = pathVariables;
        this.formValues = formValues;
        this.payload = payload;
    }

    /**
     * @return the request method
     */
    public String method() {
        return method;
    }

    /**
     * @return the request path
     */
    public String path() {
        return path;
    }

    /**
     * <p>contains the request header fields either received
     * by the server or to be sent by the client.</p>
     *
     * @return value sent by client. see {@link Headers}
     */
    public Headers headers() {
        return headers;
    }

    /**
     * <p>contains the request path variables (or params).</p>
     * <p>for example for url "/user/:id" we would have</p>
     * <p>{@code GET /user/1234/}</p>
     * <p>{@code pathVariables().get("id") == "1234"}</p>
     *
     * @return path variables sent by client. see {@link PathVariables}
     */
    public PathVariables pathVariables() {
        return pathVariables;
    }

    /**
     * <p>contains the request path variables (or params).</p>
     * <p>for example for url "/path" we would have</p>
     * <p>{@code GET /path?id=1234&name=Manu&value=}</p>
     * <p>{@code query().get("id") == "1234"}</p>
     * <p>{@code query().get("name") == "Manu"}</p>
     * <p>{@code query().get("value") == ""}</p>
     * <p>{@code query().get("wtf") == ""}}</p>
     *
     * @return query parameters sent by client. see {@link Query}
     */
    public Query query() {
        return query;
    }

    /**
     * @return the form values of the request
     * @implNote if the request's content-type header does not contain the following value {@link io.github.lazyboy81.espresso.core.http.constants.MediaType#APPLICATION_FORM_URLENCODED}
     * an empty form value will be returned, meaning {@code FormValues.isEmpty() == true}
     */
    public FormValues formValue() {
        return formValues;
    }

    public InputStream asInputStream() {
        return new ByteArrayInputStream(payload);
    }

    public <T> T json(Class<T> targetType) {
        return Bindings.json().bind(payload, targetType);
    }

    public <T> T xml(Class<T> targetType) {
        return Bindings.xml().bind(payload, targetType);
    }

    public String text() {
        return new String(payload, StandardCharsets.UTF_8);
    }

    public byte[] raw() {
        return Arrays.copyOf(payload, payload.length);
    }

    public RequestBuilder toBuilder() {
        RequestBuilder requestBuilder = new RequestBuilder();

        if (method != null) {
            requestBuilder.method(this.method);
        }

        if (path != null) {
            requestBuilder.path(this.path);
        }

        if (headers != null) {
            requestBuilder.headers(this.headers);
        }

        if (query != null) {
            requestBuilder.query(this.query);
        }

        if (pathVariables != null) {
            requestBuilder.pathVariables(this.pathVariables);
        }

        if (formValues != null) {
            requestBuilder.formValues(this.formValues);
        }

        if (payload != null) {
            requestBuilder.payload(this.payload);
        }

        return requestBuilder;
    }

    public static class RequestBuilder {

        private String method;
        private String path;
        private Headers headers;
        private Query query;
        private PathVariables pathVariables;
        private FormValues formValues;
        private byte[] payload;

        public static RequestBuilder newBuilder() {
            return new RequestBuilder();
        }

        public RequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RequestBuilder path(String path) {
            this.path = path;
            return this;
        }

        public RequestBuilder headers(Headers headers) {
            this.headers = headers;
            return this;
        }

        public RequestBuilder query(Query query) {
            this.query = query;
            return this;
        }

        public RequestBuilder pathVariables(PathVariables pathVariables) {
            this.pathVariables = pathVariables;
            return this;
        }

        public RequestBuilder formValues(FormValues formValues) {
            this.formValues = formValues;
            return this;
        }

        public RequestBuilder payload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        public Request build() {
            return new Request(this.method, this.path, this.headers, this.query, this.pathVariables, this.formValues, this.payload);
        }

    }

}
