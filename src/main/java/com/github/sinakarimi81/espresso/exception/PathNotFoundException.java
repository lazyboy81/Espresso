package com.github.sinakarimi81.espresso.exception;

import lombok.Getter;

@Getter
public class PathNotFoundException extends RuntimeException {

    private String path;

    public PathNotFoundException() {
    }

    public PathNotFoundException(String message, String path) {
        super(message);
        this.path = path;
    }
}
