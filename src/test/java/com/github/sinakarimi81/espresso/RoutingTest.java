package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpMethod;
import com.github.sinakarimi81.espresso.routing.PathNode;
import com.github.sinakarimi81.espresso.routing.RoutingGroup;
import com.github.sinakarimi81.espresso.routing.RoutingGroups;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RoutingTest {

    @Test
    public void addNewRootRoute() {
        RoutingGroups groups = RoutingGroups.getInstance();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        PathNode root = groups.getGroup(HttpMethod.GET_METHOD).getRoot();
        assertThat(root.getChildren()).isEmpty();
        assertThat(root.getHandler()).isNotNull();
    }

    @Test
    public void addNewRoute() {
        RoutingGroups groups = RoutingGroups.getInstance();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD, "/events", context -> System.out.println("/events route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add", context -> System.out.println("/add route"));

        assertThat(groups.getGroup(HttpMethod.GET_METHOD).getRoot().getChildren()).hasSize(2);
    }

    @Test
    public void addNewRouteToChild() {
        RoutingGroups groups = RoutingGroups.getInstance();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD, "/events", context -> System.out.println("/events route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add", context -> System.out.println("/add route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/events/status", context -> System.out.println("/events/status route"));

        List<PathNode> children = groups.getGroup(HttpMethod.GET_METHOD).getRoot().getChildren();
        assertThat(children).hasSize(2);
        assertThat(children).extracting(PathNode::getChildren).extracting(List::size).contains(1, 0);
    }

    @Test
    public void addNewRouteToChildChild() {
        RoutingGroups groups = RoutingGroups.getInstance();

        groups.addRoute(HttpMethod.GET_METHOD,"/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD,"/events", context -> System.out.println("/events route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/add", context -> System.out.println("/add route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/add/id", context -> System.out.println("/add/id route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/events/status", context -> System.out.println("/events/status route"));
        groups.addRoute(HttpMethod.GET_METHOD,"/events/id", context -> System.out.println("/events/id route"));

        List<PathNode> children = groups.getGroup(HttpMethod.GET_METHOD).getRoot().getChildren();
        assertThat(children).hasSize(2);
        assertThat(children).extracting(PathNode::getChildren).extracting(List::size).contains(2, 1);
    }

    @Test
    public void addNewRouteToRoot() {
        RoutingGroups groups = RoutingGroups.getInstance();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD, "/events", context -> System.out.println("/events route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add", context -> System.out.println("/add route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add/id", context -> System.out.println("/add/id route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/events/status", context -> System.out.println("/events/status route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/events/id", context -> System.out.println("/events/id route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/remove", context -> System.out.println("/remove route"));

        List<PathNode> children = groups.getGroup(HttpMethod.GET_METHOD).getRoot().getChildren();
        assertThat(children).hasSize(3);
        assertThat(children).extracting(PathNode::getChildren).extracting(List::size).contains(2, 1, 0);
    }

    @Test
    public void getHandlerForRoot() {
        RoutingGroups groups = RoutingGroups.getInstance();

        Handler handler = System.out::println;
        groups.addRoute(HttpMethod.GET_METHOD, "/", handler);

        Handler handlerForPath = groups.getGroup(HttpMethod.GET_METHOD).getHandlerForPath("/");
        assertThat(handlerForPath).isEqualTo(handler);
    }

    @Test
    public void getHandler_multiLevelTree() {
        RoutingGroups groups = RoutingGroups.getInstance();

        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);
        groups.addRoute(HttpMethod.GET_METHOD, "/events", context -> System.out.println("/events route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add", context -> System.out.println("/add route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/add/id", context -> System.out.println("/add/id route"));
        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        groups.addRoute(HttpMethod.GET_METHOD, "/events/status", eventsStatusHandler);
        groups.addRoute(HttpMethod.GET_METHOD, "/events/id", context -> System.out.println("/events/id route"));
        groups.addRoute(HttpMethod.GET_METHOD, "/remove", context -> System.out.println("/remove route"));

        Handler handlerForPath = groups.getGroup(HttpMethod.GET_METHOD).getHandlerForPath("/events/status");
        assertThat(handlerForPath).isNotNull().isEqualTo(eventsStatusHandler).isInstanceOf(Handler.class);
    }

    @Test
    public void getHandler_throwsExceptionWhenNoHandlerIsGiven() {
        RoutingGroups groups = RoutingGroups.getInstance();
        Assertions.assertThrows(IllegalArgumentException.class, () -> groups.addRoute(HttpMethod.GET_METHOD, "/", null));
    }

    @Test
    public void getHandler_multiLevelTree_throwsExceptionWhenHasNoHandler() {
        RoutingGroups groups = RoutingGroups.getInstance();
        groups.addRoute(HttpMethod.GET_METHOD, "/", System.out::println);

        RoutingGroup getRoot = groups.getGroup(HttpMethod.GET_METHOD);
        getRoot.addRoute("/", System.out::println);
        getRoot.addRoute("/events", context -> System.out.println("/events route"));
        getRoot.addRoute("/add", context -> System.out.println("/add route"));
        getRoot.addRoute("/add/id", context -> System.out.println("/add/id route"));

        Handler eventsStatusHandler = context -> System.out.println("/events/status route");
        getRoot.addRoute("/events/status", eventsStatusHandler);

        getRoot.addRoute("/events/id", context -> System.out.println("/events/id route"));
        getRoot.addRoute("/remove", context -> System.out.println("/remove route"));

        Assertions.assertThrows(PathNotFoundException.class, () -> getRoot.getHandlerForPath("/values"));
    }

}
