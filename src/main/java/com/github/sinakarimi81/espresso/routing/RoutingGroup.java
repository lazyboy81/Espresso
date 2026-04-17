package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import com.github.sinakarimi81.espresso.util.Container;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoutingGroup {

    private String method;
    private PathNode root;

    public void addRoute(String path, Handler handler) {
        if (path.isEmpty()) { // this mean they are setting a handler for the '/' path
            root.setHandler(handler);
            return;
        }

        if (root.getChildSegments().isEmpty()) {
            var node = new PathNode(path, "/".concat(path), root, new ArrayList<>(), handler);
            root.getChildSegments().add(node);
            return;
        }

        setHandler(root, path, handler);
    }

    private void setHandler(PathNode root, String path, Handler handler) {
        var curr = root;
        var pathSegments = Arrays.stream(path.split("/")).iterator(); // to avoid an extra empty element at the beginning
        walk:
        while (pathSegments.hasNext()) {
            String pathSegment = pathSegments.next();
            List<PathNode> childSegments = curr.getChildSegments();

            for (PathNode childSegment : childSegments) {
                if (childSegment.getSegment().equals(pathSegment)) {
                    curr = childSegment;
                    continue walk;
                }
            }

            String pathAtThisPoint = curr.getPathAtThisPoint().endsWith("/") ? curr.getPathAtThisPoint() : curr.getPathAtThisPoint().concat("/");
            PathNode node = new PathNode(pathSegment, pathAtThisPoint.concat(pathSegment), curr, new ArrayList<>(), null);;
            if (!pathSegments.hasNext()) {
                node.setHandler(handler);
            }

            curr.getChildSegments().add(node);
            curr = node;
        }
    }

    // todo: find a better way to implement the tree traversal
    public Handler getHandlerForPath(String fullPath) {
        Container<Handler> container = new Container<>(null);
        traverseTree(root, fullPath, container);

        if (container.getContainee() != null) {
            return container.getContainee();
        }

        return context -> context.response().json(HttpStatus.NOT_FOUND, Map.of(
                "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                "status", HttpStatus.NOT_FOUND.code(),
                "error", HttpStatus.NOT_FOUND.description(),
                "path", String.format("%s %s", method, fullPath)
        ));
    }

    private void traverseTree(PathNode root, String fullPath, Container<Handler> container) {
        if (root.getPathAtThisPoint().equals(fullPath)) {
            container.setContainee(root.getHandler());
            return;
        }

        List<PathNode> children = root.getChildSegments();
        for (PathNode child : children) {
            traverseTree(child, fullPath, container);
            if (container.getContainee() != null) {
                return;
            }
        }
    }

}
