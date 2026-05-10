package com.github.sinakarimi81.espresso.exception;

import org.eclipse.jetty.http.HttpStatus;

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
    public HttpStatus.Code getStatus() {
        return HttpStatus.Code.BAD_REQUEST;
    }

}
