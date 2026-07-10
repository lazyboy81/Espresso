package com.github.sinakarimi81.espresso.middleware;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.time.Instant;

@Slf4j
class RequestResponseLogger implements Middleware {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public Handler handle(Handler next) {
        return (request, response) -> {
            var start = Instant.now();

            var requestLog = new StringBuilder();

            // timestamp
            String startTime = DateTimeUtil.rfc3339DateFormat(start);
            requestLog.append("time: ").append(startTime).append("\n");

            // request method and uri
            requestLog.append(TextUtil.yellow(request.method()))
                    .append(" ")
                    .append(TextUtil.cyan(request.path()))
                    .append("\n");

            // request value
            request.headers()
                    .value()
                    .stream()
                    .forEach(httpField -> requestLog.append(formatHeader(httpField)).append("\n"));

            // request payload, if it is a file, we skip, if json or xml pretty print, otherwise plain text
            requestLog.append(
                            TextUtil.magenta(
                                    formatPayload(request.text(), request.headers().getHeaderValue(HttpHeader.CONTENT_TYPE))
                            )
                    )
                    .append("\n");

            log.info("\n{}", requestLog);

            next.handle(request, response);

            var end = Instant.now();

            var responseLog = new StringBuilder();
            String endTime = DateTimeUtil.rfc3339DateFormat(end);
            // timestamp
            responseLog.append("time: ").append(endTime).append("\n");

            // response value
            response.headers()
                    .stream()
                    .forEach(httpField -> responseLog.append(formatHeader(httpField)).append("\n"));

            // response status
            var status = "status: ".concat(HttpStatus.getCode(response.status()).toString());
            responseLog.append(TextUtil.green(status)).append("\n");

            // response payload, if it is a file, we skip, if json or xml pretty print, otherwise plain text
            responseLog.append(
                            TextUtil.magenta(
                                    formatPayload(response.payload(), response.headers().get(HttpHeader.CONTENT_TYPE))
                            )
                    )
                    .append("\n");

            log.info("\n{}", responseLog);
        };
    }

    private String formatPayload(String payload, String contentTypeValue) throws JsonProcessingException {
        if (contentTypeValue == null || contentTypeValue.isBlank()) {
            return payload;
        }

        return switch (contentTypeValue) {
            case "application/json" -> jsonMapper.readTree(payload).toPrettyString();
            case "application/xml" -> xmlMapper.readTree(payload).toPrettyString();
            default -> payload;
        };
    }

    private String formatHeader(HttpField httpField) {
        var header = httpField.getName().concat(": ").concat(httpField.getValue());
        return TextUtil.green(header);
    }
}
