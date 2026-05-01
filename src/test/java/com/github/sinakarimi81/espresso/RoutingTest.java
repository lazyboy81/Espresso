package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.engine.EspressoEngine;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpMethod;
import com.github.sinakarimi81.espresso.routing.PathNode;
import com.github.sinakarimi81.espresso.routing.Router;
import com.github.sinakarimi81.espresso.routing.RouteContainer;
import com.github.sinakarimi81.espresso.routing.Routes;
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

        routes.get("/", System.out::println);

        var handlerMap = (Map<String, Handler>) getFieldValue(routes, "staticRoutes", Map.class);
        assertThat(handlerMap).containsKey("GET /");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRoute() {
        Routes routes = new Routes();

        routes.get("/", System.out::println);
        routes.get("/events", context -> System.out.println("/events route"));
        routes.get("/add", context -> System.out.println("/add route"));

        var handlerMap = (Map<String, Handler>) getFieldValue(routes, "staticRoutes", Map.class);

        assertThat(handlerMap).hasSize(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToChild() {
        Routes routes = new Routes();

        routes.get("/", System.out::println);
        routes.get("/add", context -> System.out.println("/add route"));
        routes.get("/events/:id", context -> System.out.println("/events route"));

        var staticHandlerMap = (Map<String, Handler>) getFieldValue(routes, "staticRoutes", Map.class);
        var dynamicHandlerMap = (Map<String, RouteContainer>) getFieldValue(routes, "dynamicRoutes", Map.class);

        PathNode getRoot = dynamicHandlerMap.get(HttpMethod.GET_METHOD).getRoot();
        List<PathNode> children = getRoot.getChildSegments();

        assertThat(staticHandlerMap).hasSize(2).containsKeys("GET /", "GET /add");
        assertThat(children).hasSize(1);
        assertThat(children).extracting(PathNode::getChildSegments).extracting(List::size).contains(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToChildChild() {
        Routes routes = new Routes();

        routes.get("/events/:status", context -> System.out.println("/events/status route"));
        routes.get("/events/:id", context -> System.out.println("/events/id route"));

        var handlerMap = (Map<String, RouteContainer>) getFieldValue(routes, "dynamicRoutes", Map.class);
        PathNode getRoot = handlerMap.get(HttpMethod.GET_METHOD).getRoot();

        List<PathNode> children = getRoot.getChildSegments();
        assertThat(children).hasSize(1);
        assertThat(children).extracting(PathNode::getChildSegments).extracting(List::size).contains(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToRoot() {
        Routes routes = new Routes();

        routes.get("/events/:status", context -> System.out.println("/events/status route"));
        routes.get("/events/:id", context -> System.out.println("/events/id route"));
        routes.get("/add/:name", context -> System.out.println("/add/name route"));

        var handlerMap = (Map<String, RouteContainer>) getFieldValue(routes, "dynamicRoutes", Map.class);
        PathNode getRoot = handlerMap.get(HttpMethod.GET_METHOD).getRoot();

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

        Handler handler = System.out::println;
        routes.get("/", handler);

        Handler handlerForPath = routes.getHandlerForPath("GET", "/", null); // is static
        assertThat(handlerForPath).isEqualTo(handler);
    }

    @Test
    public void getHandler_static_multiLevelTree() {
        Routes routes = new Routes();

        routes.get("/", System.out::println);
        routes.get("/events", context -> System.out.println("/events route"));
        routes.get("/add", context -> System.out.println("/add route"));
        routes.get("/add/id", context -> System.out.println("/add/id route"));
        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        routes.get("/events/status", eventsStatusHandler);
        routes.get("/events/id", context -> System.out.println("/events/id route"));
        routes.get("/remove", context -> System.out.println("/remove route"));

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
        routes.get("/", System.out::println);

        routes.get("/", System.out::println);
        routes.get("/events", context -> System.out.println("/events route"));
        routes.get("/add", context -> System.out.println("/add route"));
        routes.get("/add/id", context -> System.out.println("/add/id route"));

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        routes.get("/events/status", eventsStatusHandler);

        routes.get("/events/id", context -> System.out.println("/events/id route"));
        routes.get("/remove", context -> System.out.println("/remove route"));

        Handler notFoundHandler = routes.getHandlerForPath(HttpMethod.GET_METHOD, "/values", null);
        assertThat(notFoundHandler).isNotNull();
    }

    @Test
    public void getHandler_dynamic_multiLevelTree() {
        Routes routes = new Routes();

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        routes.get("/events/:status", eventsStatusHandler);
        routes.get("/events/:id", context -> System.out.println("/events/id route"));
        routes.get("/add/:name", context -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        Handler handlerForPath = routes.getHandlerForPath("GET", "/events/valid", pathVars);
        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
        assertThat(pathVars).isNotEmpty().containsEntry("status", "valid");
    }

    @Test
    public void getHandler_dynamic_multiLevelTree_returns404Handler() {
        Routes routes = new Routes();

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        routes.get("/events/:status", eventsStatusHandler);
        routes.get("/events/:id", context -> System.out.println("/events/id route"));
        routes.get("/add/:name", context -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        Handler notFoundHandler = routes.getHandlerForPath(HttpMethod.GET_METHOD, "/remove/values", pathVars);

        assertThat(notFoundHandler).isNotNull();
        assertThat(pathVars).isEmpty();
    }

    @Test
    public void getHandler_matches_more_specific_route() {
        Routes routes = new Routes();

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        routes.get("/events/:status", eventsStatusHandler);
        routes.get("/events/valid", eventsStatusHandler);
        routes.get("/events/:id", context -> System.out.println("/events/id route"));
        routes.get("/add/:name", context -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        Handler handlerForPath = routes.getHandlerForPath(HttpMethod.GET_METHOD, "/events/valid", pathVars);

        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
        assertThat(pathVars).isEmpty();
    }

    @Test
    public void addPathForAllMethods() throws IOException {
        EspressoEngine engine = EspressoEngine.getInstance(8080);
        Routes routes = new Routes();

        engine.any("/test", System.out::println);

        for (String method : HttpMethod.METHODS) {
            Handler handlerForPath = routes.getHandlerForPath(method, "/test", null);
            assertThat(handlerForPath).isNotNull();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createNewRouteGroup() {
        Routes routes = new Routes();

        routes.get("/events", context -> System.out.println("/events route"));

        Router authRoutes = routes.group("/auth");
        authRoutes.post("/login", System.out::println);
        authRoutes.post("/signup", System.out::println);

        Router tokenRoutes = authRoutes.group("/token");
        tokenRoutes.put("/refresh", System.out::println);

        var handlerMap = (Map<String, Handler>) getFieldValue(routes, "staticRoutes", Map.class);

        assertThat(handlerMap).hasSize(4);
        assertThat(handlerMap).containsKeys("GET /events", "POST /auth/login", "POST /auth/signup", "PUT /auth/token/refresh");
    }

    @Test
    public void createNewRouteGroupThrowsExceptionWhenInputNotValid() {
        Routes routes = new Routes();

        routes.get("/events", context -> System.out.println("/events route"));

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

        routes.get("/events", context -> System.out.println("/events route"));

        Router group = routes.group("/auth");
        assertThatThrownBy(() -> group.get("login", System.out::println))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("the given input path should start with \"/\"");
        assertThatThrownBy(() -> group.get("", System.out::println))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("the given input path cannot be null/empty");
    }

}
