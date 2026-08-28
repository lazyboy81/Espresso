package io.github.lazyboy81.espresso.core.handler;

import io.github.lazyboy81.espresso.core.http.FormValues;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.PathVariables;
import io.github.lazyboy81.espresso.core.http.Query;

public class RequestUtil {

    public static RequestBuilder toBuilder(Request request) {
        RequestBuilder requestBuilder = new RequestBuilder();

        if (request.method() != null) {
            requestBuilder.method(request.method());
        }

        if (request.path() != null) {
            requestBuilder.path(request.path());
        }

        if (request.headers() != null) {
            requestBuilder.headers(request.headers());
        }

        if (request.query() != null) {
            requestBuilder.query(request.query());
        }

        if (request.pathVariables() != null) {
            requestBuilder.pathVariables(request.pathVariables());
        }

        if (request.formValue() != null) {
            requestBuilder.formValues(request.formValue());
        }

        if (request.raw() != null) {
            requestBuilder.payload(request.raw());
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
