package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.HttpStatus;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.StringUtils;
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
public class RouteContainer {

    private String method;
    private PathNode root;

    public void addRoute(String path, Handler handler) {
        setHandler(root, path, handler);
    }

    private void setHandler(PathNode root, String path, Handler handler) {
        var curr = root;

        path = StringUtils.trimBeginSlash(path);
        var pathSegments = Arrays.stream(path.split("/")).iterator();

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
    public Handler getHandlerForPath(String fullPath, Map<String, String> pathVars) {
        var handler = traverseTree(root, fullPath, pathVars);
        if (handler != null) {
            return handler;
        }

        return context -> context.response().json(HttpStatus.NOT_FOUND, Map.of(
                "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                "status", HttpStatus.NOT_FOUND.code(),
                "error", HttpStatus.NOT_FOUND.description(),
                "path", String.format("%s %s", method, fullPath)
        ));
    }

    private Handler traverseTree(PathNode root, String fullPath, Map<String, String> pathVars) {
        var curr = root;
        var pathSegments = Arrays.stream(fullPath.substring(1).split("/")).iterator(); // to avoid an extra empty element at the beginning

        walk:
        while (pathSegments.hasNext()) {
            String pathSegment = pathSegments.next();
            List<PathNode> childSegments = curr.getChildSegments();

            for (PathNode childSegment : childSegments) {
                String segment = childSegment.getSegment();

                if (isNotDynamic(segment)) {
                    if (segment.equals(pathSegment)) {
                        curr = childSegment;
                        continue walk;
                    }
                } else {
                    String varKey = segment.substring(1);
                    pathVars.put(varKey, pathSegment);
                    curr = childSegment;
                    continue walk;
                }
            }

            // the given segment didn't match anything so we don't have it
            return null;
        }

        return curr.getHandler();
    }

    private boolean isNotDynamic(String segment) {
        return !segment.contains("*") && !segment.contains(":");
    }

}
