package com.github.sinakarimi81.espresso.util;

public record Triple<L, M, R>(L left, M middle, R right) {

    public static <L, M, R> Triple<L, M, R> of(L left, M middle, R right) {
        return new Triple<>(left, middle, right);
    }

}
