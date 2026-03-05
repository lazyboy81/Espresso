package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.routing.MethodTree;
import com.github.sinakarimi81.espresso.routing.MethodTrees;
import com.github.sinakarimi81.espresso.routing.PathNode;
import com.github.sinakarimi81.espresso.util.HttpMethods;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RoutingTest {

    @Test
    public void addNewRootRoute() {
        MethodTrees trees = new MethodTrees();
        trees.add(HttpMethods.GET);

        trees.get(HttpMethods.GET).addRoute("/", context -> {
            System.out.println(String.valueOf(context));
        });
        PathNode root = trees.get(HttpMethods.GET).getRoot();
        assertThat(root.getChildren()).isEmpty();
        assertThat(root.getHandler()).isNotNull();
    }

    @Test
    public void addNewRoute() {
        MethodTrees trees = new MethodTrees();
        trees.add(HttpMethods.GET);

        MethodTree getRoot = trees.get(HttpMethods.GET);
        getRoot.addRoute("/", context -> {
            System.out.println(String.valueOf(context));
        });

        getRoot.addRoute("/events", context -> {
            System.out.println("/events route");
        });

        getRoot.addRoute("/add", context -> {
            System.out.println("/add route");
        });

        assertThat(getRoot.getRoot().getChildren()).hasSize(2);
    }

    @Test
    public void addNewRouteToChild() {
        MethodTrees trees = new MethodTrees();
        trees.add(HttpMethods.GET);

        MethodTree getRoot = trees.get(HttpMethods.GET);
        getRoot.addRoute("/", context -> {
            System.out.println(String.valueOf(context));
        });

        getRoot.addRoute("/events", context -> {
            System.out.println("/events route");
        });

        getRoot.addRoute("/add", context -> {
            System.out.println("/add route");
        });

        getRoot.addRoute("/events/status", context -> {
            System.out.println("/events/status route");
        });

        List<PathNode> children = getRoot.getRoot().getChildren();
        assertThat(children).hasSize(2);
        assertThat(children).extracting(PathNode::getChildren).extracting(List::size).contains(1, 0);
    }

    @Test
    public void addNewRouteToChildChild() {
        MethodTrees trees = new MethodTrees();
        trees.add(HttpMethods.GET);

        MethodTree getRoot = trees.get(HttpMethods.GET);
        getRoot.addRoute("/", context -> {
            System.out.println(String.valueOf(context));
        });

        getRoot.addRoute("/events", context -> {
            System.out.println("/events route");
        });

        getRoot.addRoute("/add", context -> {
            System.out.println("/add route");
        });

        getRoot.addRoute("/add/id", context -> {
            System.out.println("/add/id route");
        });

        getRoot.addRoute("/events/status", context -> {
            System.out.println("/events/status route");
        });

        getRoot.addRoute("/events/id", context -> {
            System.out.println("/events/id route");
        });

        List<PathNode> children = getRoot.getRoot().getChildren();
        assertThat(children).hasSize(2);
        assertThat(children).extracting(PathNode::getChildren).extracting(List::size).contains(2, 1);
    }

    @Test
    public void addNewRouteToRoot() {
        MethodTrees trees = new MethodTrees();
        trees.add(HttpMethods.GET);

        MethodTree getRoot = trees.get(HttpMethods.GET);
        getRoot.addRoute("/", context -> {
            System.out.println(String.valueOf(context));
        });

        getRoot.addRoute("/events", context -> {
            System.out.println("/events route");
        });

        getRoot.addRoute("/add", context -> {
            System.out.println("/add route");
        });

        getRoot.addRoute("/add/id", context -> {
            System.out.println("/add/id route");
        });

        getRoot.addRoute("/events/status", context -> {
            System.out.println("/events/status route");
        });

        getRoot.addRoute("/events/id", context -> {
            System.out.println("/events/id route");
        });

        getRoot.addRoute("/remove", context -> {
            System.out.println("/remove route");
        });

        List<PathNode> children = getRoot.getRoot().getChildren();
        assertThat(children).hasSize(3);
        assertThat(children).extracting(PathNode::getChildren).extracting(List::size).contains(2, 1, 0);
    }

}
