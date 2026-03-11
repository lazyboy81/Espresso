package com.github.sinakarimi81.espresso;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sinakarimi81.espresso.dto.Item;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
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
    public void handlePostRequest() throws Exception {
        var mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        espresso.post("/add", context -> {
            Item item = context.request().json(Item.class);
            items.add(item);
            context.response().json(HttpStatus.OK);
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

}
