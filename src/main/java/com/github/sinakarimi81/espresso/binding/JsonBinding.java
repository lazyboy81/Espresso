package com.github.sinakarimi81.espresso.binding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import com.github.sinakarimi81.espresso.http.HttpVersion;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class JsonBinding {

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> T bind(String payload, Class<T> tClass) {
        try {
            return mapper.readValue(payload, tClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to process payload as json", e);
        }
    }

    public String jsonify(HttpStatus status, Map<String, String> responseHeaders, Map<String, Object> payload) {
        StringBuilder responseMessage = new StringBuilder();

        String content = convertPayloadToJsonString(payload);
        appendStatusLine(responseMessage, status);
        appendHeaders(responseMessage, responseHeaders, content.isBlank() ? -1 : content.getBytes(StandardCharsets.UTF_8).length);
        appendPayload(responseMessage, content);

        return responseMessage.toString();
    }

    private String convertPayloadToJsonString(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return "";
        }

        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("failed to write input object as json", e);
        }
    }

    private void appendStatusLine(StringBuilder responseMessage, HttpStatus status) {
        responseMessage.append(HttpVersion.SUPPORTED_VERSIONS.getLast()).append(" ")
                .append(status.code()).append(" ")
                .append(status.description()).append("\r\n");
    }

    private void appendHeaders(StringBuilder responseMessage, Map<String, String> responseHeaders, int contentLength) {
        if (contentLength != -1) {
            responseMessage.append("Content-Type: ").append(CONTENT_TYPE).append("\r\n");
            responseMessage.append("Content-Length: ").append(contentLength).append("\r\n");
        }
        responseMessage.append("Date: ").append(DateTimeUtil.rfc1123DateFormat(Instant.now())).append("\r\n");
        responseMessage.append("Keep-Alive: ").append("timeout=60").append("\r\n");
        responseMessage.append("Connection: ").append("keep-alive").append("\r\n");

        for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
            responseMessage.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        responseMessage.append("\r\n");
    }

    private void appendPayload(StringBuilder responseMessage, String content) {
        if (content.isBlank()) {
            return;
        }

        responseMessage.append(content);
    }

}
