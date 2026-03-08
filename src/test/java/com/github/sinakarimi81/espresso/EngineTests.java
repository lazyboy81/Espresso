package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.routing.RouteDefinition;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EngineTests {

    @Test
    public void getHandlerForUrl_returnsHandler() {
        Engine engine = Engine.getInstance();
        RouteDefinition routeDefinition = RouteDefinition.getInstance();
        routeDefinition.get("/events", context -> System.out.println("/events route"));

        Handler handlerForEndpoint = engine.getHandlerForEndpoint("GET /events HTTP/1.1");
        assertThat(handlerForEndpoint).isNotNull();
    }

    @Test
    public void getHandlerForUrl_WhenNoPath_ThrowsException() {
        Engine engine = Engine.getInstance();

        assertThrows(PathNotFoundException.class, () -> engine.getHandlerForEndpoint("GET /events HTTP/1.1"));
    }

    @Test
    public void createHeaders() {
        try (var reader = new BufferedReader(new FileReader("src/test/resources/headers_test.txt"))) {
            Engine engine = Engine.getInstance();
            Headers headers = engine.createHeaders(reader);

            assertThat(headers).isNotNull();
            assertThat(headers.getAll()).hasSize(8)
                    .containsKeys("Host", "User-Agent", "Accept", "Accept-Language", "Accept-Encoding", "Accept-Charset",
                            "Keep-Alive", "Connection");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
