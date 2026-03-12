package com.github.sinakarimi81.espresso;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sinakarimi81.espresso.dto.Item;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTests {

    @Test
    public void handleGetRequest() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();
        espresso.get("/list", context -> context.response().json(HttpStatus.OK, Map.of("items", items)));


        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            executor.shutdownNow();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.headers().map()).containsKeys("Date", "Keep-Alive", "Connection", "Content-Type", "Content-Length");
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void handleHeadRequest() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();
        espresso.head("/list", context -> context.response().json(HttpStatus.OK, Map.of("items", items)));


        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .HEAD()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<Void> result = client.send(request, HttpResponse.BodyHandlers.discarding());
            executor.shutdownNow();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.headers().map()).containsKeys("Date", "Keep-Alive", "Connection", "Content-Type", "Content-Length");
            assertThat(result.body()).isNull();
        }
    }

    @Test
    public void handlePostRequest() throws Exception {
        var mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        espresso.post("/add", context -> {
            Item item = context.request().json(Item.class);
            items.add(item);
            context.response().json(HttpStatus.CREATED, Map.of("message", "created"));
        });


        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(input)))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
            executor.shutdownNow();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.code());
            assertThat(result.headers().map()).containsKeys("Date", "Keep-Alive", "Connection", "Content-Type", "Content-Length");
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handlePostRequest_NoContentResult() throws Exception {
        var mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        espresso.post("/add", context -> {
            Item item = context.request().json(Item.class);
            items.add(item);
            context.response().json(HttpStatus.NO_CONTENT);
        });


        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(input)))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<Void> result = client.send(request, HttpResponse.BodyHandlers.discarding());
            executor.shutdownNow();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.code());
            assertThat(result.headers().map()).containsKeys("Date", "Keep-Alive", "Connection");
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handleConsecutiveRequests() throws Exception {
        var mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();
        items.add(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();

        espresso.get("/list", context -> context.response().json(HttpStatus.OK, Map.of("items", items)));

        espresso.post("/add", context -> {
            Item item = context.request().json(Item.class);
            items.add(item);
            context.response().json(HttpStatus.CREATED, Map.of("message", "created"));
        });


        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            Item input = new Item(2L, "two requests", "one post and one get request");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(input)))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.code());
            assertThat(result.headers().map()).containsKeys("Date", "Keep-Alive", "Connection", "Content-Type", "Content-Length");
            assertThat(items).isNotEmpty().contains(input);

            HttpRequest getRequest = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> getResult = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            assertThat(getResult.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(getResult.headers().map()).containsKeys("Date", "Keep-Alive", "Connection", "Content-Type", "Content-Length");
            assertThat(getResult.body()).isNotNull().isNotBlank();

            client.close();
            executor.shutdownNow();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
