package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.routing.PathNode;
import com.github.sinakarimi81.espresso.routing.RouteContainer;
import com.github.sinakarimi81.espresso.routing.Router;
import com.github.sinakarimi81.espresso.routing.Routes;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RoutingTest {

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRootRoute(){
        Routes routes = new Routes();

        routes.get("/", (request, response) -> System.out.println("test"));

        var handlerMap = (Map<String, Handler>) getFieldValue(routes, "staticRoutes", Map.class);
        assertThat(handlerMap).containsKey("GET /");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRoute() {
        Routes routes = new Routes();

        routes.get("/", (request, response) -> System.out.println("test"));
        routes.get("/events", (request, response) -> System.out.println("/events route"));
        routes.get("/add", (request, response) -> System.out.println("/add route"));

        var handlerMap = (Map<String, Handler>) getFieldValue(routes, "staticRoutes", Map.class);

        assertThat(handlerMap).hasSize(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToChild() {
        Routes routes = new Routes();

        routes.get("/", (request, response) -> System.out.println("test"));
        routes.get("/add", (request, response) -> System.out.println("/add route"));
        routes.get("/events/:id", (request, response) -> System.out.println("/events route"));

        var staticHandlerMap = (Map<String, Handler>) getFieldValue(routes, "staticRoutes", Map.class);
        var dynamicHandlerMap = (Map<String, RouteContainer>) getFieldValue(routes, "dynamicRoutes", Map.class);

        PathNode getRoot = dynamicHandlerMap.get(HttpMethod.GET.asString()).getRoot();
        List<PathNode> children = getRoot.getChildSegments();

        assertThat(staticHandlerMap).hasSize(2).containsKeys("GET /", "GET /add");
        assertThat(children).hasSize(1);
        assertThat(children).extracting(PathNode::getChildSegments).extracting(List::size).contains(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToChildChild() {
        Routes routes = new Routes();

        routes.get("/events/:status", (request, response) -> System.out.println("/events/status route"));
        routes.get("/events/:id", (request, response) -> System.out.println("/events/id route"));

        var handlerMap = (Map<String, RouteContainer>) getFieldValue(routes, "dynamicRoutes", Map.class);
        PathNode getRoot = handlerMap.get(HttpMethod.GET.asString()).getRoot();

        List<PathNode> children = getRoot.getChildSegments();
        assertThat(children).hasSize(1);
        assertThat(children).extracting(PathNode::getChildSegments).extracting(List::size).contains(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToRoot() {
        Routes routes = new Routes();

        routes.get("/events/:status", (request, response) -> System.out.println("/events/status route"));
        routes.get("/events/:id", (request, response) -> System.out.println("/events/id route"));
        routes.get("/add/:name", (request, response) -> System.out.println("/add/name route"));

        var handlerMap = (Map<String, RouteContainer>) getFieldValue(routes, "dynamicRoutes", Map.class);
        PathNode getRoot = handlerMap.get(HttpMethod.GET.asString()).getRoot();

        List<PathNode> children = getRoot.getChildSegments();
        assertThat(children).hasSize(2);
        assertThat(children).extracting(PathNode::getChildSegments).extracting(List::size).contains(2, 1);
    }

    private <T> T getFieldValue(Routes obj, String name, Class<T> fieldType) {
        try {
            Field field = Routes.class.getDeclaredField(name);
            field.setAccessible(true);
            return fieldType.cast(field.get(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getHandlerForRoot() {
        Routes routes = new Routes();

        Handler handler = (request, response) -> System.out.println("test");
        routes.get("/", handler);

        Handler handlerForPath = routes.getHandlerForPath("GET", "/", null); // is static
        assertThat(handlerForPath).isEqualTo(handler);
    }

    @Test
    public void getHandler_static_multiLevelTree() {
        Routes routes = new Routes();

        routes.get("/", (request, response) -> System.out.println("test"));
        routes.get("/events", (request, response) -> System.out.println("/events route"));
        routes.get("/add", (request, response) -> System.out.println("/add route"));
        routes.get("/add/id", (request, response) -> System.out.println("/add/id route"));
        Handler eventsStatusHandler = (request, response) -> System.out.println("/events/status route");
        routes.get("/events/status", eventsStatusHandler);
        routes.get("/events/id", (request, response) -> System.out.println("/events/id route"));
        routes.get("/remove", (request, response) -> System.out.println("/remove route"));

        Handler handlerForPath = routes.getHandlerForPath("GET", "/events/status", null);
        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
    }

    @Test
    public void getHandler_throwsExceptionWhenNoHandlerIsGiven() {
        Routes groups = new Routes();
        Assertions.assertThrows(IllegalArgumentException.class, () -> groups.get("/", null));
    }

    @Test
    public void getHandler_static_multiLevelTree_throwsExceptionWhenHasNoHandler() {
        Routes routes = new Routes();
        routes.get("/", (request, response) -> System.out.println("test"));

        routes.get("/", (request, response) -> System.out.println("test"));
        routes.get("/events", (request, response) -> System.out.println("/events route"));
        routes.get("/add", (request, response) -> System.out.println("/add route"));
        routes.get("/add/id", (request, response) -> System.out.println("/add/id route"));

        Handler eventsStatusHandler = (request, response) -> System.out.println("/events/status route");
        routes.get("/events/status", eventsStatusHandler);

        routes.get("/events/id", (request, response) -> System.out.println("/events/id route"));
        routes.get("/remove", (request, response) -> System.out.println("/remove route"));

        assertThatThrownBy(() -> routes.getHandlerForPath(HttpMethod.GET.asString(), "/values", new HashMap<>()))
                .isInstanceOf(PathNotFoundException.class);
    }

    @Test
    public void getHandler_dynamic_multiLevelTree() {
        Routes routes = new Routes();

        Handler eventsStatusHandler = (request, response) -> System.out.println("/events/status route");
        routes.get("/events/:status", eventsStatusHandler);
        routes.get("/events/:id", (request, response) -> System.out.println("/events/id route"));
        routes.get("/add/:name", (request, response) -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        Handler handlerForPath = routes.getHandlerForPath("GET", "/events/valid", pathVars);
        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
        assertThat(pathVars).isNotEmpty().containsEntry("status", "valid");
    }

    @Test
    public void getHandler_dynamic_multiLevelTree_ThrowsExceptionWhenNoHandlerFound() {
        Routes routes = new Routes();

        Handler eventsStatusHandler = (request, response) -> System.out.println("/events/status route");
        routes.get("/events/:status", eventsStatusHandler);
        routes.get("/events/:id", (request, response) -> System.out.println("/events/id route"));
        routes.get("/add/:name", (request, response) -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        assertThatThrownBy(() -> routes.getHandlerForPath(HttpMethod.GET.asString(), "/remove/values", pathVars))
                .isInstanceOf(PathNotFoundException.class);

        assertThat(pathVars).isEmpty();
    }

    @Test
    public void getHandler_matches_more_specific_route() {
        Routes routes = new Routes();

        Handler eventsStatusHandler = (request, response) -> System.out.println("/events/status route");
        routes.get("/events/:status", eventsStatusHandler);
        routes.get("/events/valid", eventsStatusHandler);
        routes.get("/events/:id", (request, response) -> System.out.println("/events/id route"));
        routes.get("/add/:name", (request, response) -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        Handler handlerForPath = routes.getHandlerForPath(HttpMethod.GET.asString(), "/events/valid", pathVars);

        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
        assertThat(pathVars).isEmpty();
    }

    @Test
    public void addPathForAllMethods() {
        Routes routes = new Routes();

        List<String> methods = List.of(
                HttpMethod.OPTIONS.asString(), HttpMethod.HEAD.asString(), HttpMethod.GET.asString(),
                HttpMethod.POST.asString(), HttpMethod.PUT.asString(), HttpMethod.DELETE.asString()
        );

        routes.any("/test", (request, response) -> System.out.println("test"));

        for (String method : methods) {
            Handler handlerForPath = routes.getHandlerForPath(method, "/test", new HashMap<>());
            assertThat(handlerForPath).isNotNull();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createNewRouteGroup() {
        Routes routes = new Routes();

        routes.get("/events", (request, response) -> System.out.println("/events route"));

        Router authRoutes = routes.group("/auth");
        authRoutes.post("/login", (request, response) -> System.out.println("test"));
        authRoutes.post("/signup", (request, response) -> System.out.println("test"));

        Router tokenRoutes = authRoutes.group("/token");
        tokenRoutes.put("/refresh", (request, response) -> System.out.println("test"));

        var handlerMap = (Map<String, Handler>) getFieldValue(routes, "staticRoutes", Map.class);

        assertThat(handlerMap).hasSize(4);
        assertThat(handlerMap).containsKeys("GET /events", "POST /auth/login", "POST /auth/signup", "PUT /auth/token/refresh");
    }

    @Test
    public void createNewRouteGroupThrowsExceptionWhenInputNotValid() {
        Routes routes = new Routes();

        routes.get("/events", (request, response) -> System.out.println("/events route"));

        assertThatThrownBy(() -> routes.group("auth"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("the given input path should start with \"/\"");
        assertThatThrownBy(() -> routes.group(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("the given input path cannot be null/empty");
    }

    @Test
    public void createNewRouteGroupThrowsExceptionWhenAddingPath() {
        Routes routes = new Routes();

        routes.get("/events", (request, response) -> System.out.println("/events route"));

        Router group = routes.group("/auth");
        assertThatThrownBy(() -> group.get("login", (request, response) -> System.out.println("test")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("the given input path should start with \"/\"");
        assertThatThrownBy(() -> group.get("", (request, response) -> System.out.println("test")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("the given input path cannot be null/empty");
    }

}
