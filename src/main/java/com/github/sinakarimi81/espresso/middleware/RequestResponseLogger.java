package com.github.sinakarimi81.espresso.middleware;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sinakarimi81.espresso.exception.EspressoException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;

import java.time.Instant;

@Slf4j
class RequestResponseLogger implements Middleware {

    @Override
    public Handler handle(Handler next) {
        return (request, response) -> {
            var start = Instant.now();
            var requestLog = new LogBuffer().startTime(start)
                    .endpoint(request.method(), request.path())
                    .headers(request.headers().value())
                    .payload(request.text(), request.headers().getHeaderValue(HttpHeader.CONTENT_TYPE));
            log.info("\n{}", requestLog);

            try {
                next.handle(request, response);
            } catch (Exception e) {
                var end = Instant.now();
                var errorLog = new LogBuffer().endTime(end).headers(response.headers());

                if (e instanceof EspressoException ee) {
                    errorLog.status(ee.getStatus().getCode());
                } else {
                    errorLog.status(500);
                }

                errorLog.payload(e.getMessage(), "application/text");

                log.error("\n{}", errorLog);
                throw e;
            }

            var end = Instant.now();
            var responseLog = new LogBuffer().endTime(end)
                    .headers(response.headers())
                    .status(response.status())
                    .payload(response.payload(), response.headers().get(HttpHeader.CONTENT_TYPE));
            log.info("\n{}", responseLog);
        };
    }

    private static class LogBuffer {
        private final StringBuilder log;
        private static final ObjectMapper jsonMapper = new ObjectMapper();
        private static final XmlMapper xmlMapper = new XmlMapper();

        public LogBuffer() {
            log = new StringBuilder();
        }

        public LogBuffer startTime(Instant instant) {
            logTime("start", instant);
            return this;
        }

        public LogBuffer endTime(Instant instant) {
            logTime("end", instant);
            return this;
        }

        public LogBuffer endpoint(String method, String path) {
            log.append(TextUtil.yellow(method))
                    .append(" ")
                    .append(TextUtil.cyan(path))
                    .append("\n");
            return this;
        }

        public LogBuffer headers(HttpFields value) {
            value.stream().forEach(httpField -> log.append(formatHeader(httpField)).append("\n"));
            return this;
        }

        // payload, if it is a file, we skip, if json or xml pretty print, otherwise plain text
        public LogBuffer payload(String text, String headerValue) throws JsonProcessingException {
            log.append(TextUtil.magenta(formatPayload(text, headerValue))).append("\n");
            return this;
        }

        public LogBuffer status(int statusCode) {
            var status = "status: ".concat(HttpStatus.getCode(statusCode).toString());
            log.append(TextUtil.green(status)).append("\n");
            return this;
        }

        public String toString() {
            return log.toString();
        }

        private void logTime(String type, Instant instant) {
            String time = DateTimeUtil.rfc3339DateFormat(instant);
            log.append(type).append("-time: ").append(time).append("\n");
        }

        private String formatHeader(HttpField httpField) {
            var header = httpField.getName().concat(": ").concat(httpField.getValue());
            return TextUtil.green(header);
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

    }
}
