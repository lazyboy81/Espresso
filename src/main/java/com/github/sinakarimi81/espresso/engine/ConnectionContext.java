package com.github.sinakarimi81.espresso.engine;

import com.github.sinakarimi81.espresso.exception.VersionNotSupportedException;
import com.github.sinakarimi81.espresso.http.FormValues;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.http.MediaType;
import com.github.sinakarimi81.espresso.http.Query;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.sinakarimi81.espresso.http.HttpVersion.SUPPORTED_VERSIONS;

@Slf4j
public class ConnectionContext {

    private static final int BUFFER_SIZE = 4096;
    private final ByteBuffer buffer;
    private final StringBuilder messagePayload;
    @Getter
    private ConnectionState state;
    private int contentBodyRead = 0;

    @Getter
    private String method = "";
    @Getter
    private String path = "";
    @Getter
    private String query = "";

    @Getter
    private Headers headers;
    @Getter
    private Query queryParams;
    @Getter
    private FormValues formValues;

    public ConnectionContext() {
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.messagePayload = new StringBuilder();
        this.state = ConnectionState.PARSING_HEADER;
    }

    public String getBody() {
        return messagePayload.toString();
    }

    public int readFromChannel(SocketChannel channel) throws IOException {
        int read = channel.read(buffer);
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        messagePayload.append(new String(data, StandardCharsets.UTF_8));
        buffer.clear();

        return read;
    }

    public void parseUrlAndHeaders() {
        parseUrlAndQueryParams();

        headers = new Headers();

        int eoh = messagePayload.indexOf("\r\n\r\n"); // to get the headers
        String headerPart = messagePayload.substring(0, eoh);

        for (String header : headerPart.split("\r\n")) {
            String[] keyValue = header.split(": ");
            if (keyValue[1].contains(";")) {
                for (String val : keyValue[1].split(";")) {
                    headers.addHeader(keyValue[0], val);
                }
            } else {
                headers.addHeader(keyValue[0], keyValue[1]);
            }
        }

        messagePayload.delete(0, eoh + 4); // so the next time we fill request builder only body is left, 4 is \r\n\r\n length
        if (!messagePayload.isEmpty()) {
            // so we can be sure we have read then body with the correct length
            contentBodyRead = messagePayload.length();
        }

        validateHeaders();
        this.state = ConnectionState.PARSING_BODY;
    }

    private void parseUrlAndQueryParams() {
        int eol = messagePayload.indexOf("\r\n", 0);// to get the method, path and version
        if (eol <= 0) {
            return;
        }

        String requestLine = messagePayload.substring(0, eol);
        String[] requestLineParts = requestLine.split(" ");
        method = requestLineParts[0];
        path = requestLineParts[1];

        String version = requestLineParts[2];
        if (!SUPPORTED_VERSIONS.contains(version)) {
            log.error("version {} is not supported by Espresso", version);
            throw new VersionNotSupportedException(String.format("version %s is not supported by Espresso", version));
        }

        if (path.contains("?")) {
            query = path.substring(path.indexOf("?") + 1);
            path = path.substring(0, path.indexOf("?"));
            parseQueryParams();
        }

        messagePayload.delete(0, eol + 2); // so next we have to only parse headers, 2 is \r\n length
    }

    private void parseQueryParams() {
        var params = new HashMap<String, String>();

        if (query.isEmpty()) {
            queryParams = new Query(params);
            return;
        }

        for (String param : query.split("&")) {
            String[] keyValue = param.split("=");
            // handles the case where the query is like key= (basically a key is present with no value)
            params.put(keyValue[0], keyValue.length != 2 && param.indexOf("=") != 0 ? "" : keyValue[1]);
        }

        queryParams = new Query(params);
    }

    private void validateHeaders() {
        if (headers.containsHeader("Upgrade")) {
            List<String> upgrade = headers.getHeader("Upgrade");
            if (upgrade.contains("h2c")) {
                log.error("request contains Upgrade: h2c header. http version 2 is not supported by Espresso");
                throw new VersionNotSupportedException("request contains Upgrade: h2c header, http version 2 is not supported by Espresso");
            }
        }
    }

    //TODO: another branch for chunked encoding
    public void readAndParseBody(SocketChannel channel) throws IOException {
        int lengthBodyShouldHave = contentLengthHeader();
        if (!hasAlreadyReadBody()) {
            int remaining = lengthBodyShouldHave - contentBodyRead;

            while (remaining > 0) {
                int read = readFromChannel(channel);
                if (read <= 0) return;

                remaining -= read;
                contentBodyRead += read;
            }
        }

        String body = messagePayload.toString();
        if (headers.hasValue("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            var params = new HashMap<String, List<String>>();
            for (String pair : body.split("&")) {
                // regex is applied once, meaning the result only has two elements -> key and value
                String[] kv = pair.split("=", 2);

                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1
                        ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                        : "";

                if (value.isBlank()) continue;

                params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }

            formValues = new FormValues(params);
            messagePayload.setLength(0); // so no more body
        }

        this.state = ConnectionState.PROCESSING_REQUEST;
    }

    public boolean hasNoBody() {
        return contentLengthHeader() <= 0;
    }

    public boolean hasAlreadyReadBody() {
        return this.contentBodyRead >= contentLengthHeader();
    }

    private int contentLengthHeader() {
        var contentLength = headers.getHeader("Content-Length");
        return contentLength.isEmpty() ? 0 : Integer.parseInt(contentLength.getFirst()); // since it should have one element
    }

    public void reset() {
        buffer.position(0);
        messagePayload.setLength(0);
        queryParams = null;
        headers = null;
        formValues = null;
        contentBodyRead = -1;
        method = "";
        path = "";
        query = "";
    }

}
