package com.github.sinakarimi81.espresso.exception;

import org.eclipse.jetty.http.HttpStatus;

public class PathNotFoundException extends EspressoException {

    public PathNotFoundException() {
    }

    public PathNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus.Code getStatus() {
        return HttpStatus.Code.NOT_FOUND;
    }

}
