package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.context.Context;
import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.parsing.ParsingUtils;
import com.github.sinakarimi81.espresso.routing.RouteDefinition;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EngineTests {

    @Test
    public void getHandlerForUrl_returnsHandler() throws IOException {
        Engine engine = Engine.getInstance();
        RouteDefinition routeDefinition = RouteDefinition.getInstance();
        routeDefinition.get("/events", context -> System.out.println("/events route"));

        Handler handlerForEndpoint = engine.getHandlerForEndpoint(new BufferedReader(new FileReader("src/test/resources/url_test.txt")));
        assertThat(handlerForEndpoint).isNotNull();
    }

    @Test
    public void getHandlerForUrl_WhenNoPath_ThrowsException() {
        Engine engine = Engine.getInstance();

        assertThrows(PathNotFoundException.class, () -> engine.getHandlerForEndpoint(new BufferedReader(new FileReader("src/test/resources/url_test.txt"))));
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

    @Test
    public void createContext() {
        try (var reader = new BufferedReader(new FileReader("src/test/resources/request.txt"))) {
            Engine engine = Engine.getInstance();
            RouteDefinition routeDefinition = RouteDefinition.getInstance();

            routeDefinition.post("/events", context -> {
                assertThat(context).isNotNull();
                assertThat(context.getRequest()).isNotNull();
                assertThat(context.getRequest().getHeaders()).isNotNull();
                assertThat(context.getResponse()).isNotNull();
            });

            Handler handlerForEndpoint = engine.getHandlerForEndpoint(reader);
            Headers headers = engine.createHeaders(reader);
            Context context = engine.createContext(headers, ParsingUtils.getPayload(reader), null);

            assertThat(handlerForEndpoint).isNotNull();

            assertThat(headers).isNotNull();
            assertThat(headers.getAll()).hasSize(8)
                    .containsKeys("Host", "User-Agent", "Accept", "Accept-Language", "Accept-Encoding", "Accept-Charset",
                            "Keep-Alive", "Connection");

            handlerForEndpoint.handle(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
