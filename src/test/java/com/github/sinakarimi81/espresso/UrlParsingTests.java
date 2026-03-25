package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.dto.Item;
import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlParsingTests {

    @Test
    public void queryParamsMustBeParsed() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        espresso.get("/list", context -> {
            assertThat(context.request().query().params()).isNotNull()
                    .isNotEmpty()
                    .containsKeys("name", "age", "verified")
                    .containsValues("sina", "23", "");
            context.response().json(HttpStatus.OK, Map.of("items", items));
        });

        try (var executor = Executors.newCachedThreadPool();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list?name=sina&age=23&verified="))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            client.shutdownNow();
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void queryParamsMustEmpty() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        espresso.get("/list", context -> {
            assertThat(context.request().query().params()).isNotNull().isEmpty();
            context.response().json(HttpStatus.OK, Map.of("items", items));
        });

        try (var executor = Executors.newCachedThreadPool();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            client.shutdownNow();
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

}
