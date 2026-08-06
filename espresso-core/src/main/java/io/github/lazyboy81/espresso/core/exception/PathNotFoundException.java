package io.github.lazyboy81.espresso.core.exception;


import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;

public class PathNotFoundException extends EspressoException {

    public PathNotFoundException() {
    }

    public PathNotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

}
