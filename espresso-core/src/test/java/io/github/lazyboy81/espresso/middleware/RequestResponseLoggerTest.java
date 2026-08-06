package io.github.lazyboy81.espresso.middleware;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.github.lazyboy81.espresso.config.ListAppender;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.handler.Request;
import io.github.lazyboy81.espresso.core.handler.Response;
import io.github.lazyboy81.espresso.core.http.Headers;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
        try {
            var middleware = new RequestResponseLogger();

            Handler endpoint = (request, response) -> response.text(HttpStatus.Code.OK, "Test");

            Handler decorated = middleware.handle(endpoint);

            Request request = createTestRequest();
            Response response = createTestResponse(200);

            decorated.handle(request, response);

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
                    .containsEntry("status", "[200 OK]")
                    .containsEntry("body", "Test");
        } catch (Exception e) {
            fail("test failed due to exception", e);
        }
    }

    @Test
    public void middlewareLogsRequestAndResponse_withError() {
        try {
            var middleware = new RequestResponseLogger();

            Handler endpoint = (request, response) -> {
                throw new RuntimeException("test exception");
            };

            Handler decorated = middleware.handle(endpoint);

            Request request = createTestRequest();
            Response response = createTestResponse(500); // no need for it here

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
                    .containsEntry("status", "[500 Server Error]");
        } catch (Exception e) {
            fail("test failed due to exception", e);
        }
    }

    private Request createTestRequest() throws IOException {
        HttpFields.Mutable httpFields = HttpFields.build();
        httpFields.put("X-Request-ID", "<sample-request-id>");
        httpFields.put("Content-Type", "application/json");
        httpFields.put("Accept", "application/json");
        httpFields.put("Host", "localhost");
        var header = new Headers(httpFields);

        return new Request("GET", "/", header, null, null, null, InputStream.nullInputStream());
    }

    private Response createTestResponse(int statusCode) {
        return new Response(new MockResponse(statusCode), Callback.NOOP);
    }

    private record MockResponse(int status) implements org.eclipse.jetty.server.Response {

            @Override
            public org.eclipse.jetty.server.Request getRequest() {
                return null;
            }

            @Override
            public int getStatus() {
                return status;
            }

            @Override
            public void setStatus(int code) {

            }

            @Override
            public HttpFields.Mutable getHeaders() {
                return HttpFields.build();
            }

            @Override
            public Supplier<HttpFields> getTrailersSupplier() {
                return null;
            }

            @Override
            public void setTrailersSupplier(Supplier<HttpFields> trailers) {

            }

            @Override
            public boolean isCommitted() {
                return false;
            }

            @Override
            public boolean hasLastWrite() {
                return false;
            }

            @Override
            public boolean isCompletedSuccessfully() {
                return false;
            }

            @Override
            public void reset() {

            }

            @Override
            public CompletableFuture<Void> writeInterim(int status, HttpFields headers) {
                return null;
            }

            @Override
            public void write(boolean last, ByteBuffer byteBuffer, Callback callback) {

            }
        }

}
