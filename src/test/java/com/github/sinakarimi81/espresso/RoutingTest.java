package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpMethod;
import com.github.sinakarimi81.espresso.routing.PathNode;
import com.github.sinakarimi81.espresso.routing.RoutingGroup;
import com.github.sinakarimi81.espresso.routing.RoutingGroups;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RoutingTest {

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRootRoute(){
        RoutingGroups groups = new RoutingGroups();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);

        var handlerMap = (Map<String, Handler>) getFieldValue(groups, "staticGroups", Map.class);
        assertThat(handlerMap).containsKey("GET /");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRoute() {
        RoutingGroups groups = new RoutingGroups();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD, "/events", context -> System.out.println("/events route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add", context -> System.out.println("/add route"));

        var handlerMap = (Map<String, Handler>) getFieldValue(groups, "staticGroups", Map.class);

        assertThat(handlerMap).hasSize(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToChild() {
        RoutingGroups groups = new RoutingGroups();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD, "/add", context -> System.out.println("/add route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/events/:id", context -> System.out.println("/events route"));

        var staticHandlerMap = (Map<String, Handler>) getFieldValue(groups, "staticGroups", Map.class);
        var dynamicHandlerMap = (Map<String, RoutingGroup>) getFieldValue(groups, "dynamicGroups", Map.class);

        PathNode getRoot = dynamicHandlerMap.get(HttpMethod.GET_METHOD).getRoot();
        List<PathNode> children = getRoot.getChildSegments();

        assertThat(staticHandlerMap).hasSize(2).containsKeys("GET /", "GET /add");
        assertThat(children).hasSize(1);
        assertThat(children).extracting(PathNode::getChildSegments).extracting(List::size).contains(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToChildChild() {
        RoutingGroups groups = new RoutingGroups();

        groups.addRoute(HttpMethod.GET_METHOD,"/events/:status", context -> System.out.println("/events/status route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/events/:id", context -> System.out.println("/events/id route"));

        var handlerMap = (Map<String, RoutingGroup>) getFieldValue(groups, "dynamicGroups", Map.class);
        PathNode getRoot = handlerMap.get(HttpMethod.GET_METHOD).getRoot();

        List<PathNode> children = getRoot.getChildSegments();
        assertThat(children).hasSize(1);
        assertThat(children).extracting(PathNode::getChildSegments).extracting(List::size).contains(2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addNewRouteToRoot() {
        RoutingGroups groups = new RoutingGroups();

        groups.addRoute(HttpMethod.GET_METHOD,"/events/:status", context -> System.out.println("/events/status route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/events/:id", context -> System.out.println("/events/id route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/add/:name", context -> System.out.println("/add/name route"));

        var handlerMap = (Map<String, RoutingGroup>) getFieldValue(groups, "dynamicGroups", Map.class);
        PathNode getRoot = handlerMap.get(HttpMethod.GET_METHOD).getRoot();

        List<PathNode> children = getRoot.getChildSegments();
        assertThat(children).hasSize(2);
        assertThat(children).extracting(PathNode::getChildSegments).extracting(List::size).contains(2, 1);
    }

    private <T> T getFieldValue(RoutingGroups obj, String name, Class<T> fieldType) {
        try {
            Field field = RoutingGroups.class.getDeclaredField(name);
            field.setAccessible(true);
            return fieldType.cast(field.get(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getHandlerForRoot() {
        RoutingGroups groups = new RoutingGroups();

        Handler handler = System.out::println;
        groups.addRoute(HttpMethod.GET_METHOD, "/", handler);

        Handler handlerForPath = groups.getHandlerForPath("GET", "/", null); // is static
        assertThat(handlerForPath).isEqualTo(handler);
    }

    @Test
    public void getHandler_static_multiLevelTree() {
        RoutingGroups groups = new RoutingGroups();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD, "/events", context -> System.out.println("/events route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add", context -> System.out.println("/add route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add/id", context -> System.out.println("/add/id route"));
        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        groups.addRoute(HttpMethod.GET_METHOD, "/events/status", eventsStatusHandler);
        groups.addRoute(HttpMethod.GET_METHOD, "/events/id", context -> System.out.println("/events/id route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/remove", context -> System.out.println("/remove route"));

        Handler handlerForPath = groups.getHandlerForPath("GET", "/events/status", null);
        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
    }

    @Test
    public void getHandler_throwsExceptionWhenNoHandlerIsGiven() {
        RoutingGroups groups = new RoutingGroups();
        Assertions.assertThrows(IllegalArgumentException.class, () -> groups.addRoute(HttpMethod.GET_METHOD, "/", null));
    }

    @Test
    public void getHandler_static_multiLevelTree_throwsExceptionWhenHasNoHandler() {
        RoutingGroups groups = new RoutingGroups();
        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD, "/events", context -> System.out.println("/events route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add", context -> System.out.println("/add route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add/id", context -> System.out.println("/add/id route"));

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        groups.addRoute(HttpMethod.GET_METHOD, "/events/status", eventsStatusHandler);

        groups.addRoute(HttpMethod.GET_METHOD, "/events/id", context -> System.out.println("/events/id route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/remove", context -> System.out.println("/remove route"));

        Handler notFoundHandler = groups.getHandlerForPath(HttpMethod.GET_METHOD, "/values", null);
        assertThat(notFoundHandler).isNotNull();
    }

    @Test
    public void getHandler_dynamic_multiLevelTree() {
        RoutingGroups groups = new RoutingGroups();

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        groups.addRoute(HttpMethod.GET_METHOD,"/events/:status", eventsStatusHandler);
        groups.addRoute(HttpMethod.GET_METHOD,"/events/:id", context -> System.out.println("/events/id route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/add/:name", context -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        Handler handlerForPath = groups.getHandlerForPath("GET", "/events/valid", pathVars);
        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
        assertThat(pathVars).isNotEmpty().containsEntry("status", "valid");
    }

    @Test
    public void getHandler_dynamic_multiLevelTree_returns404Handler() {
        RoutingGroups groups = new RoutingGroups();

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        groups.addRoute(HttpMethod.GET_METHOD,"/events/:status", eventsStatusHandler);
        groups.addRoute(HttpMethod.GET_METHOD,"/events/:id", context -> System.out.println("/events/id route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/add/:name", context -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        Handler notFoundHandler = groups.getHandlerForPath(HttpMethod.GET_METHOD, "/remove/values", pathVars);

        assertThat(notFoundHandler).isNotNull();
        assertThat(pathVars).isEmpty();
    }

    @Test
    public void getHandler_matches_more_specific_route() {
        RoutingGroups groups = new RoutingGroups();

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        groups.addRoute(HttpMethod.GET_METHOD,"/events/:status", eventsStatusHandler);
        groups.addRoute(HttpMethod.GET_METHOD,"/events/valid", eventsStatusHandler);
        groups.addRoute(HttpMethod.GET_METHOD,"/events/:id", context -> System.out.println("/events/id route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/add/:name", context -> System.out.println("/add/name route"));

        var pathVars = new HashMap<String, String>();
        Handler handlerForPath = groups.getHandlerForPath(HttpMethod.GET_METHOD, "/events/valid", pathVars);

        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
        assertThat(pathVars).isEmpty();
    }

    @Test
    public void addPathForAllMethods() throws IOException {
        Engine engine = Engine.getInstance(8080);
        RoutingGroups groups = new RoutingGroups();

        engine.any("/test", System.out::println);

        for (String method : HttpMethod.METHODS) {
            Handler handlerForPath = groups.getHandlerForPath(method, "/test", null);
            assertThat(handlerForPath).isNotNull();
        }
    }

}
