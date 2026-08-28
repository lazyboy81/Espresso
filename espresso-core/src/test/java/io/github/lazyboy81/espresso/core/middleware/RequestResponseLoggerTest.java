package io.github.lazyboy81.espresso.core.middleware;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.github.lazyboy81.espresso.core.config.ListAppender;
import io.github.lazyboy81.espresso.core.engine.ResponseChannel;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.handler.Request;
import io.github.lazyboy81.espresso.core.handler.RequestUtil;
import io.github.lazyboy81.espresso.core.handler.ResponseImpl;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.*;

public class RequestResponseLoggerTest {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(RequestResponseLogger.class);

    private static ListAppender appender;

    @BeforeAll
    static void startLogger() {
        LoggerContext context =
                (LoggerContext) LoggerFactory.getILoggerFactory();

        appender = new ListAppender();
        appender.setName("request-response-test-appender");
        appender.setContext(context);
        appender.start();

        logger.detachAndStopAllAppenders();
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);
    }

    @BeforeEach
    void clearLogsBeforeTest() {
        appender.clear();
    }

    @AfterEach
    void clearLogsAfterTest() {
        appender.clear();
    }

    @AfterAll
    static void stopLogger() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    public void middlewareLogsRequestAndResponse() {
        var middleware = new RequestResponseLogger();

        Handler endpoint = (request, response) -> response.text(HttpStatus.OK, "Test");

        Handler decorated = middleware.handle(endpoint);

        Request request = createTestRequest();
        ResponseImpl response = createTestResponse(200);

        assertThatNoException().isThrownBy(() -> decorated.handle(request, response));

        assertThat(appender.getEvents()).hasSize(2);
        // request
        String requestLog = appender.getEvents().getFirst().getFormattedMessage();
        assertThatJson(requestLog)
                .isObject()
                .containsKeys("start-time", "method", "path");
        // response
        String responseLog = appender.getEvents().getLast().getFormattedMessage();
        assertThatJson(responseLog)
                .isObject()
                .containsKeys("end-time", "status", "body")
                .containsEntry("status", "200 OK")
                .containsEntry("body", "Test");
    }

    @Test
    public void middlewareLogsRequestAndResponse_withError() {
        var middleware = new RequestResponseLogger();

        Handler endpoint = (request, response) -> {
            throw new RuntimeException("test exception");
        };

        Handler decorated = middleware.handle(endpoint);

        Request request = createTestRequest();
        ResponseImpl response = createTestResponse(500); // no need for it here

        // because we rethrow the exception
        assertThatThrownBy(() -> decorated.handle(request, response));

        assertThat(appender.getEvents()).hasSize(2);
        // request
        String requestLog = appender.getEvents().getFirst().getFormattedMessage();
        assertThatJson(requestLog)
                .isObject()
                .containsKeys("start-time", "method", "path");
        // response
        String responseLog = appender.getEvents().getLast().getFormattedMessage();
        assertThatJson(responseLog)
                .isObject()
                .containsKeys("end-time", "status", "body")
                .containsEntry("body", "test exception")
                .containsEntry("status", "500 INTERNAL_SERVER_ERROR");
    }

    private Request createTestRequest() {
        Map<String, String> httpFields = new HashMap<>();
        httpFields.put("X-Request-ID", "<sample-request-id>");
        httpFields.put("Content-Type", "application/json");
        httpFields.put("Accept", "application/json");
        httpFields.put("Host", "localhost");
        var header = new Headers(httpFields);

        return RequestUtil.RequestBuilder.newBuilder()
                .method("GET")
                .path("/")
                .headers(header)
                .payload(new byte[0])
                .build();
    }

    private ResponseImpl createTestResponse(int statusCode) {
        return new ResponseImpl(new MockResponseChannel(statusCode));
    }

    private record MockResponseChannel(int status) implements ResponseChannel {

        @Override
        public int status() {
            return status;
        }

        @Override
        public void status(int status) {

        }

        @Override
        public void setHeader(String name, String value) {

        }

        @Override
        public void removeHeader(String name) {

        }

        @Override
        public Headers getHeaders() {
            return new Headers(new HashMap<>());
        }

        @Override
        public void write(byte[] body) {

        }

        @Override
        public boolean committed() {
            return false;
        }

        @Override
        public void fail(Throwable failure) {

        }

        @Override
        public byte[] capturedPayload() {
            return "Test".getBytes(StandardCharsets.UTF_8);
        }
    }

}
