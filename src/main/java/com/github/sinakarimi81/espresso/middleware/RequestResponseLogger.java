package com.github.sinakarimi81.espresso.middleware;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.exception.EspressoException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.TextUtil;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            var responseLog = new LogBuffer()
                    .endTime(end)
                    .headers(response.headers())
                    .status(response.status())
                    .payload(response.payload(), response.headers().get(HttpHeader.CONTENT_TYPE));
            log.info("\n{}", responseLog);
        };
    }

    private static class LogBuffer {
        private final ObjectNode logNode;
        private static final ObjectMapper jsonMapper = new ObjectMapper();
        private static final XmlMapper xmlMapper = new XmlMapper();

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

        public LogBuffer headers(HttpFields value) {
            value.stream().forEach(httpField -> logNode.put(httpField.getName(), TextUtil.green(httpField.getValue())));
            return this;
        }

        // payload, if it is a file, we skip, if json or xml pretty print, otherwise plain text
        public LogBuffer payload(String text, String headerValue) throws JsonProcessingException {
            if (text == null || text.isBlank()) {
                return this;
            }

            formatPayload(text, headerValue);
            return this;
        }

        public LogBuffer status(int statusCode) {
            logNode.put("status", HttpStatus.getCode(statusCode).toString());
            return this;
        }

        public String toString() {
            return logNode.toPrettyString();
        }

        private void logTime(String type, Instant instant) {
            String time = DateTimeUtil.rfc3339DateFormat(instant);
            logNode.put(type + "-time", time);
        }

        private void formatPayload(String payload, String contentTypeValue) throws JsonProcessingException {
            if (Bindings.json().canHandle(contentTypeValue)) {
                logNode.set("body", jsonMapper.readTree(payload));
            } else {
                logNode.put("body", payload);
            }
        }

    }
}
