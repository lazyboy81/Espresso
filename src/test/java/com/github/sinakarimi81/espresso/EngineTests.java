package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.routing.RouteDefinition;
import org.junit.jupiter.api.Test;

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

}
