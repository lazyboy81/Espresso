package io.github.lazyboy81.espresso.core.util;

public record Tuple<L, R>(L left,
                          R right) {

    public static <L, R> Tuple<L, R> of(L left, R right) {
        return new Tuple<>(left, right);
    }

}
