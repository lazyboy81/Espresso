package io.github.lazyboy81.espresso.core.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.lazyboy81.espresso.core.binding.Bindings;
import io.github.lazyboy81.espresso.core.exception.EspressoException;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.constants.HttpHeader;
import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;
import io.github.lazyboy81.espresso.core.util.DateTimeUtil;
import io.github.lazyboy81.espresso.core.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

class RequestResponseLogger implements Middleware {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLogger.class);

    @Override
    public Handler handle(Handler next) {
        return (request, response) -> {
            var start = Instant.now();
            var requestLog = new LogBuffer()
                    .startTime(start)
                    .endpoint(request.method(), request.path())
                    .headers(request.headers())
                    .payload(request.raw(), request.headers().getHeaderValue(HttpHeader.CONTENT_TYPE.value()));
            log.info("{}\n", requestLog);

            try {
                next.handle(request, response);
            } catch (Exception e) {
                var end = Instant.now();
                var errorLog = new LogBuffer().endTime(end).headers(response.headers());

                if (e instanceof EspressoException ee) {
                    errorLog.status(ee.getStatus().value());
                } else {
                    errorLog.status(500);
                }

                errorLog.payload(e.getMessage().getBytes(StandardCharsets.UTF_8), "application/text");

                log.error("{}\n", errorLog);
                throw e;
            }

            var end = Instant.now();
            var responseLog = new LogBuffer()
                    .endTime(end)
                    .headers(response.headers())
                    .status(response.status())
                    .payload(response.capturedPayload(), response.headers().getHeaderValue(HttpHeader.CONTENT_TYPE.value()));
            log.info("\n{}", responseLog);
        };
    }

    private static class LogBuffer {
        private final ObjectNode logNode;
        private static final ObjectMapper jsonMapper = new ObjectMapper();

        public LogBuffer() {
            logNode = jsonMapper.createObjectNode();
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
            logNode.put("method", TextUtil.yellow(method));
            logNode.put("path", TextUtil.cyan(path));
            return this;
        }

        public LogBuffer headers(Headers headers) {
            headers.value().forEach((key, val) -> logNode.put(key, TextUtil.green(val)));
            return this;
        }

        // payload, if it is a file, we skip, if json or xml pretty print, otherwise plain text
        public LogBuffer payload(byte[] payload, String headerValue) throws IOException {
            if (payload == null || payload.length == 0) {
                return this;
            }

            formatPayload(payload, headerValue);
            return this;
        }

        public LogBuffer status(int statusCode) {
            logNode.put("status", HttpStatus.valueOf(statusCode).toString());
            return this;
        }

        public String toString() {
            return logNode.toPrettyString();
        }

        private void logTime(String type, Instant instant) {
            String time = DateTimeUtil.rfc3339DateFormat(instant);
            logNode.put(type + "-time", time);
        }

        private void formatPayload(byte[] payload, String contentTypeValue) throws IOException {
            if (Bindings.json().canHandle(contentTypeValue)) {
                logNode.set("body", jsonMapper.readTree(payload));
            } else {
                logNode.put("body", new String(payload));
            }
        }

    }
}
