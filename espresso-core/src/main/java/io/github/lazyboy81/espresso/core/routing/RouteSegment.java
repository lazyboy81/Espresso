package io.github.lazyboy81.espresso.core.routing;

import java.util.Objects;

sealed interface RouteSegment permits LiteralSegment, ParameterSegment, CatchAllSegment {

    int score();

}

record LiteralSegment(String value) implements RouteSegment {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LiteralSegment(String that))) return false;
        return Objects.equals(value, that);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public int score() {
        return 3;
    }
}

record ParameterSegment(String name) implements RouteSegment {
    @Override
    public boolean equals(Object o) {
        return o instanceof ParameterSegment(String that);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public int score() {
        return 2;
    }
}

record CatchAllSegment() implements RouteSegment {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CatchAllSegment;
    }

    @Override
    public int score() {
        return 1;
    }
}
