package com.github.sinakarimi81.espresso.parsing;

import com.github.sinakarimi81.espresso.Espresso;
import com.github.sinakarimi81.espresso.dto.Item;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class UrlParsingTests {

    @Test
    public void queryParamsMustBeParsed() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();

        espresso.get("/list", (request, response) -> {
            Fields params = request.query().params();
            assertThat(params).isNotNull().isNotEmpty();
            assertThat(params.getNames()).contains("name", "age", "verified");
            assertThat(params.getValue("name")).isEqualTo("sina");
            assertThat(params.getValue("age")).isEqualTo("23");
            assertThat(params.getValue("verified")).isBlank();

            response.json(HttpStatus.Code.OK, Map.of("items", items));
        });

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list?name=sina&age=23&verified="))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.OK.getCode());
            assertThat(result.body()).isNotNull().isNotBlank();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    public void queryParamsMustEmpty() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();

        espresso.get("/list", (request, response) -> {
            assertThat(request.query().params()).isNotNull().isEmpty();

            response.json(HttpStatus.Code.OK, Map.of("items", items));
        });

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.OK.getCode());
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void handleRequestWithPathVariables() throws Exception {
        Espresso espresso = Espresso.getDefault();

        espresso.get("/event/:status", (request, response) -> {
            String status = request.pathVariables().get("status");

            String message = String.format("received request for status: %s", status);

            response.json(HttpStatus.Code.OK, Map.of("message", message));
        });

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/event/valid"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));


            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.OK.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("message", "received request for status: valid");
        }
    }

    @Test
    public void handleRequestWithQueryParam() throws Exception {
        Espresso espresso = Espresso.getDefault();

        espresso.get("/event", (request, response) -> {
            String status = request.query().get("status");

            String message = String.format("received request for status: %s", status);

            response.json(HttpStatus.Code.OK, Map.of("message", message));
        });

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/event?status=valid"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.OK.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject().containsEntry("message", "received request for status: valid");
        }
    }

}
