package io.github.lazyboy81.espresso.core.exception;


import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;

public class BadRequestException extends EspressoException {

    public BadRequestException() {
    }

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }

}
