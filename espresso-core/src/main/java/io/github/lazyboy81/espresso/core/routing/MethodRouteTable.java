package io.github.lazyboy81.espresso.core.routing;

import io.github.lazyboy81.espresso.core.exception.AmbiguousPathException;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.http.PathVariables;

import java.util.*;
import java.util.stream.Collectors;

final class MethodRouteTable {

    private final Map<String, RouteEntry> exactRoutes;
    private final Queue<RouteEntry> patternRoutes;

    MethodRouteTable() {
        this.exactRoutes = new HashMap<>();
        this.patternRoutes = new PriorityQueue<>(new EntryComparator());
    }

    public void addPath(String path, Handler handler) {
        if (isNotDynamic(path)) {
            List<RouteSegment> segments = splitPath(path)
                    .stream()
                    .map(LiteralSegment::new)
                    .collect(Collectors.toList());

            var pattern = new RoutePattern(path, segments);

            RouteEntry value = new RouteEntry(pattern, handler);
            exactRoutes.put(path, value);
            return;
        }

        var segments = createSegments(splitPath(path));

        var pattern = new RoutePattern(path, segments);

        RouteEntry value = new RouteEntry(pattern, handler);

        checkIfAmbiguous(value);

        patternRoutes.add(value);
    }

    private void checkIfAmbiguous(RouteEntry value) {
        for (RouteEntry patternRoute : patternRoutes) {
            var isAmbiguous = patternRoute.pattern().equals(value.pattern());
            if (isAmbiguous) {
                String message = String.format(
                        "the given path: '%s' is in conflict with already existing path: '%s'",
                        patternRoute.pattern().source(),
                        value.pattern().source()
                );
                throw new AmbiguousPathException(message);
            }
        }
    }

    public Optional<RouteMatch> match(String path) {
        RouteEntry exact = exactRoutes.get(path);

        if (exact != null) {
            return Optional.of(new RouteMatch(
                    new PathVariables(Map.of()),
                    exact.handler()
            ));
        }

        List<String> pathSegments = splitPath(path);

        for (RouteEntry entry : patternRoutes) {
            Optional<PatternMatch> patternMatch = entry.pattern().match(pathSegments);

            if (patternMatch.isPresent()) {
                return Optional.of(new RouteMatch(
                        new PathVariables(patternMatch.get().pathParameters()),
                        entry.handler()
                ));
            }
        }

        return Optional.empty();
    }

    private boolean isNotDynamic(String path) {
        return !path.contains("*") && !path.contains(":");
    }

    private List<String> splitPath(String path) {
        String normalizePath = normalizePath(path);
        return Arrays.asList(normalizePath.split("/"));
    }

    private String normalizePath(String path) {
        return path.substring(1);
    }

    private List<RouteSegment> createSegments(List<String> splitPath) {
        List<RouteSegment> segments = new ArrayList<>();

        for (String s : splitPath) {
            if (s.contains(":")) {
                segments.add(new ParameterSegment(s.substring(1))); // so only name is stored
                continue;
            }

            if (s.equals("*")) {
                segments.add(new CatchAllSegment());
                continue;
            }

            segments.add(new LiteralSegment(s));
        }

        return segments;
    }

}
