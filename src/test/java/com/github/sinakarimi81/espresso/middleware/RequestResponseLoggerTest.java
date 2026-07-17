package com.github.sinakarimi81.espresso.middleware;

import com.github.sinakarimi81.espresso.Espresso;
import com.github.sinakarimi81.espresso.config.ListAppender;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class RequestResponseLoggerTest {

    @BeforeEach
    void clearLogEvents() {
        ListAppender.clear();
    }

    private record TestResp(String message, int version) {}

    @Test
    public void middlewareLogsRequestAndResponse() {
        var espresso = Espresso.getDefault();
        espresso.get("/", (request, response) -> response.text(HttpStatus.Code.OK, "Test"));
        espresso.use(Middlewares.requestResponseLogger());

        var request = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8080/")).build();
        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
        } catch (Exception e) {
            fail("test request id failed", e);
        }
    }

    @Test
    public void middlewareLogsRequestAndResponse_withError() {
        var espresso = Espresso.getDefault();
        espresso.get("/", (request, response) -> {
            throw new RuntimeException("test exception");
        });
        espresso.use(Middlewares.requestResponseLogger());

        var request = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8080/")).build();
        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
        } catch (Exception e) {
            fail("test request id failed", e);
        }
    }

}
