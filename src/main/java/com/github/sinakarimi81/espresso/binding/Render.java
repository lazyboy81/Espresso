package com.github.sinakarimi81.espresso.binding;

import com.github.sinakarimi81.espresso.http.HttpMethod;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import com.github.sinakarimi81.espresso.http.HttpVersion;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public abstract class Render {

    public abstract String convertResponsePayload(Object payload);
    public abstract String contentTypeValue();

    public String serialize(String requestMethod, HttpStatus status, Map<String, String> responseHeaders, Object payload) {
        StringBuilder responseMessage = new StringBuilder();

        String content = convertResponsePayload(payload);
        appendStatusLine(responseMessage, status);
        appendHeaders(responseMessage, responseHeaders, content.isBlank() ? -1 : content.getBytes(StandardCharsets.UTF_8).length);
        if (!HttpMethod.HEAD_METHOD.equals(requestMethod) || status != HttpStatus.NO_CONTENT) {
            appendPayload(responseMessage, content);
        }

        return responseMessage.toString();
    }


    protected void appendStatusLine(StringBuilder responseMessage, HttpStatus status) {
        responseMessage.append(HttpVersion.SUPPORTED_VERSIONS.getLast()).append(" ")
                .append(status.code()).append(" ")
                .append(status.description()).append("\r\n");
    }

    protected void appendHeaders(StringBuilder responseMessage, Map<String, String> responseHeaders, int contentLength) {
        if (contentLength != -1) {
            responseMessage.append("Content-Type: ").append(contentTypeValue()).append("\r\n");
            responseMessage.append("Content-Length: ").append(contentLength).append("\r\n");
        }
        responseMessage.append("Date: ").append(DateTimeUtil.rfc1123DateFormat(Instant.now())).append("\r\n");
        boolean doesNotContainConnection = !responseHeaders.containsKey("Connection");
        boolean connectionValueIsKeepAlive = !doesNotContainConnection && responseHeaders.get("Connection").equals("keep-alive");
        boolean doesNotContainKeepAlive = !responseHeaders.containsKey("Keep-Alive");

        if (doesNotContainConnection) responseMessage.append("Connection: ").append("keep-alive").append("\r\n");
        if (doesNotContainKeepAlive && connectionValueIsKeepAlive) responseMessage.append("Keep-Alive: ").append("timeout=60").append("\r\n");

        for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
            responseMessage.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        responseMessage.append("\r\n");
    }

    protected void appendPayload(StringBuilder responseMessage, String content) {
        if (content.isBlank()) {
            return;
        }

        responseMessage.append(content);
    }


}
