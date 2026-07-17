package com.github.sinakarimi81.espresso.middleware;

import com.github.sinakarimi81.espresso.Espresso;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class RequestIdGeneratorTest {

    @Test
    public void testRequestId() {
        var espresso = Espresso.getDefault();
        espresso.get("/", (request, response) -> response.text(HttpStatus.Code.OK, "Test"));
        espresso.use(Middlewares.requestId());

        var request = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8080/")).build();
        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().map().get("X-Request-ID")).isNotEmpty();
        } catch (Exception e) {
            fail("test request id failed", e);
        }
    }

    @Test
    public void testRequestId_WhenHeaderExistsInRequest() {
        var espresso = Espresso.getDefault();
        espresso.get("/", (request, response) -> response.text(HttpStatus.Code.OK, "Test"));
        espresso.use(Middlewares.requestId());

        var request = HttpRequest.newBuilder().GET()
                .uri(URI.create("http://localhost:8080/"))
                .setHeader("X-Request-ID", "<sample-request-id>")
                .build();
        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().map().get("X-Request-ID")).containsOnly("<sample-request-id>");
        } catch (Exception e) {
            fail("test request id failed", e);
        }
    }

}
