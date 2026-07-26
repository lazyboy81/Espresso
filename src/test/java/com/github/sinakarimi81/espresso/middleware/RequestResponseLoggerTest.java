package com.github.sinakarimi81.espresso.middleware;

import com.github.sinakarimi81.espresso.Espresso;
import com.github.sinakarimi81.espresso.config.ListAppender;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class RequestResponseLoggerTest {

    private static Espresso espresso;
    private static ExecutorService serverExecutor;
    private static Future<?> serverTask;

    @BeforeAll
    static void startServer() throws Exception {
        espresso = Espresso.getDefault();

        espresso.get("/success", (request, response) ->
                response.text(HttpStatus.Code.OK, "Test"));

        espresso.get("/error", (request, response) -> {
            throw new RuntimeException("test exception");
        });

        // Register exactly once.
        espresso.use(Middlewares.requestResponseLogger());

        serverExecutor = Executors.newSingleThreadExecutor();
        serverTask = serverExecutor.submit(espresso::start);
    }

    @AfterEach
    void clearLogsAfterTest() {
        ListAppender.clear();
    }

    @AfterAll
    static void stopServer() throws Exception {
        espresso.shutdown();

        // start() returns after server.join() is released by stop().
        serverTask.get(5, TimeUnit.SECONDS);

        serverExecutor.shutdownNow();
    }

    @Test
    public void middlewareLogsRequestAndResponse() {
        sendRequest("/success");

        assertThat(ListAppender.events).hasSize(2);
        // request
        String requestLog = ListAppender.events.getFirst().getFormattedMessage();
        assertThatJson(requestLog)
                .isObject()
                .containsKeys("start-time", "method", "path");
        // response
        String responseLog = ListAppender.events.getLast().getFormattedMessage();
        assertThatJson(responseLog)
                .isObject()
                .containsKeys("end-time", "status", "body")
                .containsEntry("status", "[200 OK]")
                .containsEntry("body", "Test");
    }

    @Test
    public void middlewareLogsRequestAndResponse_withError() {
        sendRequest("/error");

        assertThat(ListAppender.events).hasSize(2);
        // request
        String requestLog = ListAppender.events.getFirst().getFormattedMessage();
        assertThatJson(requestLog)
                .isObject()
                .containsKeys("start-time", "method", "path");
        // response
        String responseLog = ListAppender.events.getLast().getFormattedMessage();
        assertThatJson(responseLog)
                .isObject()
                .containsKeys("end-time", "status", "body")
                .containsEntry("body", "test exception")
                .containsEntry("status", "[500 Server Error]");
    }

    private void sendRequest(String path) {
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080" + path))
                .build();

        try (var client = HttpClient.newHttpClient()) {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new AssertionError("Test request failed", e);
        }
    }

}
