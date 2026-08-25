package io.github.lazyboy81.espresso.starter.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;
import io.github.lazyboy81.espresso.jetty.JettyOptions;
import io.github.lazyboy81.espresso.starter.Espresso;
import io.github.lazyboy81.espresso.core.binding.dto.TemplateData;
import io.github.lazyboy81.espresso.starter.dto.Item;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTests {

    private static Espresso espresso;
    private static ExecutorService serverExecutor;

    @BeforeAll
    static void startServer() {
        espresso = new Espresso(JettyOptions.defaultOps());

        // json output
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));
        espresso.get("/list", (request, response) -> response.json(HttpStatus.OK, Map.of("items", items)));

        // HTML output
        espresso.get("/hello", (request, response) -> response.html(HttpStatus.OK, TemplateData.builder().name("index").build()));

        serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.submit(espresso::start);
    }

    @AfterAll
    static void stopServer() {
        espresso.shutdown();
        serverExecutor.shutdownNow();
    }

    @Test
    public void handleGetRequest() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void handleGetRequest_HTML() throws Exception {
        try (var client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/hello"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));


            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/html"));
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void handleHeadRequest() throws Exception {
        espresso.head("/list", (request, response) -> response.json(HttpStatus.OK, Map.of("items", List.of())));

        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .HEAD()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<Void> result = client.send(request, HttpResponse.BodyHandlers.discarding());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.body()).isNull();
        }
    }

    @Test
    public void handlePostRequest_JSON() throws Exception {
        List<Item> items = new ArrayList<>();

        espresso.post("/add-json", (request, response) -> {
            Item item = request.json(Item.class);
            items.add(item);
            response.json(HttpStatus.CREATED, Map.of("message", "created"));
        });

        try (var client = HttpClient.newHttpClient()) {
            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("{\"id\": 1, \"name\": \"create server\", \"description\": \"create an http server\"}"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add-json"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handlePostRequest_XML() throws Exception {
        List<Item> items = new ArrayList<>();

        espresso.post("/add-xml", (request, response) -> {
            Item item = request.xml(Item.class);
            items.add(item);
            response.xml(HttpStatus.CREATED, Map.of("message", "created"));
        });

        try (var client = HttpClient.newHttpClient()) {
            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("<Item><id>1</id><name>create server</name><description>create an http server</description></Item>"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add-xml"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/xml"));
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handlePostRequest_Text() throws Exception {
        List<String> items = new ArrayList<>();

        espresso.post("/add-text", (request, response) -> {
            String text = request.text();
            items.add(text);
            response.text(HttpStatus.CREATED, "created");
        });

        try (var client = HttpClient.newHttpClient()) {
            String input = "test text input";
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(input))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add-text"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/plain"));
            assertThat(items).isNotEmpty().contains(input);
            assertThat(result.body()).isNotNull().isNotBlank().isEqualTo("created");

        }
    }

    @Test
    public void handleFormValueSentToServer() throws Exception {
        List<String> checkItems = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> roles = new ArrayList<>();

        espresso.post("/form", (request, response) -> {
            List<String> tags = request.formValue().getValues("tag");
            checkItems.addAll(tags);
            names.add(request.formValue().get("name"));
            roles.add(request.formValue().get("role"));
            response.json(HttpStatus.CREATED, tags);
        });

        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("tag=java&tag=nio&tag=http&name=John+Doe&role=admin+%26+developer&key="))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/form"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(checkItems).isNotEmpty().containsExactlyInAnyOrder("java", "nio", "http");
            assertThat(names).isNotEmpty().contains("John Doe");
            assertThat(roles).isNotEmpty().contains("admin & developer");
        }
    }

    @Test
    public void handlePostRequest_NoContentResult() throws Exception {
        var mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();

        espresso.post("/add-no-content", (request, response) -> {
            Item item = request.json(Item.class);
            items.add(item);
            response.json(HttpStatus.NO_CONTENT);
        });


        try (var client = HttpClient.newHttpClient()) {
            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(input)))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add-no-content"))
                    .build();

            // this is intentional to make sure that no body is sent
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
            assertThat(result.body()).isBlank();
            assertThat(result.headers().map()).containsKeys("Date");
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    @Disabled
    public void handleConsecutiveRequests() throws Exception {
        espresso.get("/map", (request, response) -> response.json(HttpStatus.OK, Map.of("ok", true)));

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
                                                    .uri(URI.create("http://localhost:8080/map"))
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

        }
    }

    @Test
    public void handlePathNotFoundException() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/not-found"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("status", 404)
                    .containsEntry("error", "Not Found")
                    .containsEntry("message", "Path 'GET /not-found' was not found");
        }
    }

    @Test
    public void handle5xxException() throws Exception {
        espresso.get("/error", (request, response) -> {
            throw new Exception("throws exception");
        });

        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/error"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));


            assertThat(result.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("status", 500)
                    .containsEntry("error", "Server Error")
                    .containsEntry("message", "throws exception");
        }
    }

    @Test
    public void handlePathNotFoundException_returnHTML() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .setHeader("Accept", "text/html")
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/html-not-found"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(result.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
            assertThat(result.headers().map()).containsKeys("Date", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/html"));
            assertThat(result.body()).isNotNull().isNotBlank().containsSubsequence("404 – Not Found");
        }
    }

}
