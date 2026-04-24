package com.github.sinakarimi81.espresso;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.sinakarimi81.espresso.dto.Item;
import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.Executors;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class IntegrationTests {

    @Test
    public void handleGetRequest() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        espresso.get("/list", context -> context.response().json(HttpStatus.OK, Map.of("items", items)));

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
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void handleGetRequest_HTML() throws Exception {
        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        espresso.get("/hello", context -> context.response().html(HttpStatus.OK, "index"));

        try (var executor = Executors.newCachedThreadPool();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/hello"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            client.shutdownNow();
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/html; charset=utf-8"));
            assertThat(result.body()).isNotNull().isNotBlank();
        }
    }

    @Test
    public void handleHeadRequest() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);
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
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThat(result.body()).isNull();
        }
    }

    @Test
    public void handlePostRequest_JSON() throws Exception {
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);
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
                    .POST(HttpRequest.BodyPublishers.ofString("{\"id\": 1, \"name\": \"create server\", \"description\": \"create an http server\"}"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handlePostRequest_XML() throws Exception {
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);
        espresso.post("/add", context -> {
            Item item = context.request().xml(Item.class);
            items.add(item);
            context.response().xml(HttpStatus.CREATED, Map.of("message", "created"));
        });


        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("<Item><id>1</id><name>create server</name><description>create an http server</description></Item>"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("application/xml; charset=utf-8"));
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handlePostRequest_Text() throws Exception {
        List<Item> items = new ArrayList<>();

        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);
        espresso.post("/add", context -> {
            String text = context.request().text();
            XmlMapper m = new XmlMapper();
            Item item = m.readValue(text, Item.class);
            items.add(item);
            context.response().text(HttpStatus.CREATED, "created");
        });


        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            Item input = new Item(1L, "create server", "create an http server");
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("<Item><id>1</id><name>create server</name><description>create an http server</description></Item>"))
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/add"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThat(result.headers().map()).containsEntry("Content-Type", List.of("text/plain; charset=utf-8"));
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
        Engine engine = Engine.getInstance(8080);
        espresso.post("/form", context -> {
            List<String> tags = context.request().formValue("tag");
            checkItems.addAll(tags);
            names.add(context.request().formValue("name").getFirst());
            roles.add(context.request().formValue("role").getFirst());
            context.response().json(HttpStatus.CREATED, tags);
        });


        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString("tag=java&tag=nio&tag=http&name=John+Doe&role=admin+%26+developer&key="))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/form"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.CREATED.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
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
        Engine engine = Engine.getInstance(8080);

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
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection");
            assertThat(items).isNotEmpty().contains(input);
        }
    }

    @Test
    public void handleConsecutiveRequests() throws Exception {
        var mapper = new ObjectMapper();
        List<Item> items = new ArrayList<>();
        items.add(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

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
            engine.stop();
        } catch (Exception e) {
            log.error("e: ", e);
        }
    }

    @Test
    public void handlePathNotFoundException() throws Exception {
        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
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
            assertThat(result.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("status", 404)
                    .containsEntry("error", "Not Found")
                    .containsEntry("path", "GET /list");
        }
    }

    @Test
    public void handleVersionNotSupportedException() throws Exception {
        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_2)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            client.shutdownNow();
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("status", 400)
                    .containsEntry("error", "Bad Request")
                    .containsEntry("message", "request contains Upgrade: h2c header, http version 2 is not supported by Espresso");
        }
    }

    @Test
    public void handle5xxException() throws Exception {
        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        espresso.get("/list", context -> {
            throw new Exception("throws exception");
        });

        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
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
            assertThat(result.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("status", 500)
                    .containsEntry("error", "Internal Server Error")
                    .containsEntry("message", "throws exception");
        }
    }

    @Test
    public void closeConnectionAfterKeepAlivePasses() throws Exception {
        List<Item> items = List.of(new Item(1L, "create server", "create an http server"));

        Espresso espresso = Espresso.getDefault();

        espresso.get("/list", context -> context.response().json(HttpStatus.OK, Map.of("items", items)));

        var executor = Executors.newCachedThreadPool();
        try (var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .setHeader("Keep-Alive", "10")
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThat(result.body()).isNotNull().isNotBlank();

            Thread.sleep(Duration.ofSeconds(15));
            request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/list"))
                    .build();
            result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Map<String, List<String>> headers = result.headers().map();
            assertThat(headers).containsKey("Connection").doesNotContainKey("Keep-Alive").containsValue(List.of("close"));
        } finally {
            espresso.close();
            executor.shutdownNow();
        }
    }

    @Test
    public void handleRequestWithPathVariables() throws Exception {
        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        espresso.get("/event/:status", context -> {
            String status = context.request().pathVariables().get("status");

            String message = String.format("received request for status: %s", status);

            context.response().json(HttpStatus.OK, Map.of("message", message));
        });

        try (var executor = Executors.newCachedThreadPool();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/event/valid"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            client.shutdownNow();
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject()
                    .containsEntry("message", "received request for status: valid");
        }
    }

    @Test
    public void handleRequestWithQueryParam() throws Exception {
        Espresso espresso = Espresso.getDefault();
        Engine engine = Engine.getInstance(8080);

        espresso.get("/event", context -> {
            String status = context.request().query().get("status");

            String message = String.format("received request for status: %s", status);

            context.response().json(HttpStatus.OK, Map.of("message", message));
        });

        try (var executor = Executors.newCachedThreadPool();
             var client = HttpClient.newHttpClient()) {
            executor.submit(espresso::start);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(URI.create("http://localhost:8080/event?status=valid"))
                    .build();
            HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            client.shutdownNow();
            executor.shutdownNow();
            engine.stop();
            assertThat(result.statusCode()).isEqualTo(HttpStatus.OK.code());
            assertThat(result.headers().map()).containsKeys("Date", "Connection", "Content-Type", "Content-Length");
            assertThatJson(result.body()).isNotNull().isObject().containsEntry("message", "received request for status: valid");
        }
    }

}
