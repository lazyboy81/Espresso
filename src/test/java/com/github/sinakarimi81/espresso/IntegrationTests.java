package com.github.sinakarimi81.espresso;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sinakarimi81.espresso.binding.dto.TemplateData;
import com.github.sinakarimi81.espresso.dto.Item;
import com.github.sinakarimi81.espresso.middleware.Middlewares;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class IntegrationTests {

    @Test
    public void handleGetRequest() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();

        espresso.use(Middlewares.requestResponseLogger());
        espresso.get("/list", (request, response) -> response.json(HttpStatus.Code.OK, Map.of("items", items)));

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.OK.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void handleGetRequest_HTML() throws Exception {
        Espresso espresso = Espresso.getDefault();

        espresso.get("/hello", (request, response) -> response.html(HttpStatus.Code.OK, TemplateData.builder().name("index").build()));

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/hello"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));


            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.OK.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/html;charset=utf-8"));
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void handleHeadRequest() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();
        espresso.head("/list", (request, response) -> response.json(HttpStatus.Code.OK, Map.of("items", items)));


        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .HEAD()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<Void> result = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.OK.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.body()).isNull();
        }
    }

    @Test
    public void handlePostRequest_JSON() throws Exception {
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        espresso.post("/add", (request, response) -> {
            Item item = request.json(Item.class);
            items.add(item);
            response.json(HttpStatus.Code.CREATED, Map.of("message", "created"));
        });


        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"id\": 1, \"name\": \"create server\", \"description\": \"create an http server\"}"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.CREATED.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handlePostRequest_XML() throws Exception {
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        espresso.post("/add", (request, response) -> {
            Item item = request.xml(Item.class);
            items.add(item);
            response.xml(HttpStatus.Code.CREATED, Map.of("message", "created"));
        });


        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("<Item><id>1</id><name>create server</name><description>create an http server</description></Item>"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.CREATED.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/xml;charset=utf-8"));
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handlePostRequest_Text() throws Exception {
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        espresso.post("/add", (request, response) -> {
            String text = request.text();
            XmlMapper m = new XmlMapper();
            Item item = m.readValue(text, Item.class);
            items.add(item);
            response.text(HttpStatus.Code.CREATED, "created");
        });


        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("<Item><id>1</id><name>create server</name><description>create an http server</description></Item>"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.CREATED.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/plain;charset=utf-8"));
            assertThat(items).isNotEmpty().contains(input);
            assertThat(result.body()).isNotNull().isNotBlank().isEqualTo("created");

        }
    }

    @Test
    public void handleFormValueSentToServer() throws Exception {
        List<String> checkItems = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> roles = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        espresso.post("/form", (request, response) -> {
            List<String> tags = request.formValue().getValues("tag");
            checkItems.addAll(tags);
            names.add(request.formValue().get("name"));
            roles.add(request.formValue().get("role"));
            response.json(HttpStatus.Code.CREATED, tags);
        });


        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("tag=java&tag=nio&tag=http&name=John+Doe&role=admin+%26+developer&key="))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/form"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.CREATED.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(checkItems).isNotEmpty().contains("java", "nio", "http");
            assertThat(names).isNotEmpty().contains("John Doe");
            assertThat(roles).isNotEmpty().contains("admin & developer");
        }
    }

    @Test
    public void handlePostRequest_NoContentResult() throws Exception {
        var mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();

        espresso.post("/add", (request, response) -> {
            Item item = request.json(Item.class);
            items.add(item);
            response.json(HttpStatus.Code.NO_CONTENT);
        });


        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(input)))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();

            // this is intentional to make sure that no body is sent
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.NO_CONTENT.getCode());
            assertThat(result.body()).isBlank();
            assertThat(result.headers().map()).containsKeys("Date");
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    @Disabled
    public void handleConsecutiveRequests() throws Exception {
        Espresso espresso = Espresso.getDefault();

        espresso.get("/list", (request, response) -> response.json(HttpStatus.Code.OK, Map.of("ok", true)));

        try (var serverExec = Executors.newSingleThreadExecutor()) {

            serverExec.submit(espresso::start);

            Thread.sleep(Duration.ofMillis(200)); // to make sure server has started

            int parallel = 5;

            // fire 50 concurrent requests
            List<CompletableFuture<HttpResponse<String>>> futures =
                    IntStream.range(0, parallel)
                            .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                                try (var client = HttpClient.newHttpClient()) {
                                    return client.send(
                                            HttpRequest.newBuilder()
                                                    .GET()
                                                    .version(HttpClient.Version.HTTP_1_1)
                                                    .uri(URI.create("http://localhost:8080/list"))
                                                    .build(),
                                            HttpResponse.BodyHandlers.ofString()
                                    );
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }))
                            .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            for (var f : futures) {
                HttpResponse<String> res = f.get();
                assertThat(res.statusCode()).isEqualTo(200);
                assertThat(res.body()).isNotBlank();
            }

        } finally {

        }
    }

    @Test
    public void handlePathNotFoundException() throws Exception {
        Espresso espresso = Espresso.getDefault();

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.NOT_FOUND.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("status", 404)
                    .containsEntry("error", "Not Found")
                    .containsEntry("message", "no route was found for method: GET path: /list");
        }
    }

    @Test
    public void handle5xxException() throws Exception {
        Espresso espresso = Espresso.getDefault();

        espresso.get("/list", (request, response) -> {
            throw new Exception("throws exception");
        });

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));


            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.INTERNAL_SERVER_ERROR.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("status", 500)
                    .containsEntry("error", "Server Error")
                    .containsEntry("message", "throws exception");
        }
    }

    @Test
    public void handlePathNotFoundException_returnHTML() throws Exception {
        Espresso espresso = Espresso.getDefault();

        try (var client = HttpClient.newHttpClient()) {
            CompletableFuture.runAsync(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .setHeader("Accept", "text/html")
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.Code.NOT_FOUND.getCode());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/html;charset=utf-8"));
            assertThat(result.body()).isNotNull().isNotBlank().containsSubsequence("404 – Not Found");
        }
    }

}
