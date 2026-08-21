package io.github.lazyboy81.espresso.core.routing;

import java.util.*;

// RoutePattern handles only:
//  - Segment comparison
//  - Parameter extraction
//  - Catch-all extraction
public record RoutePattern(String source,
                           List<RouteSegment> segments) {

    public Optional<PatternMatch> match(List<String> pathSegments) {
        if (segments.size() < pathSegments.size() && !containsCatchAll()) {
            return Optional.empty();
        }

        if (pathSegments.size() < segments.size()) {
            return Optional.empty();
        }

        Map<String, String> pathParams = new HashMap<>();
        for (int segmentIdx = 0; segmentIdx < segments.size(); segmentIdx++) {
            if (segments.get(segmentIdx) instanceof LiteralSegment(String value)) {
                if (value.equals(pathSegments.get(segmentIdx))) {
                    continue;
                }

                return Optional.empty();
            }

            if (segments.get(segmentIdx) instanceof ParameterSegment(String name)) {
                pathParams.put(name, pathSegments.get(segmentIdx));
                continue;
            }

            if (segments.get(segmentIdx) instanceof CatchAllSegment) {
                continue;
            }
        }

        return Optional.of(new PatternMatch(pathParams));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RoutePattern that)) return false;
        return Objects.equals(segments, that.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(segments);
    }

    private boolean containsCatchAll() {
        return segments.stream().anyMatch(s -> s instanceof CatchAllSegment);
    }
}
