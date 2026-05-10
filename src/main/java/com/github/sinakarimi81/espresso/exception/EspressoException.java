package com.github.sinakarimi81.espresso.exception;

import org.eclipse.jetty.http.HttpStatus;

public abstract class EspressoException extends RuntimeException {

    public EspressoException() {
    }

    public EspressoException(String message) {
        super(message);
    }

    public EspressoException(String message, Throwable cause) {
        super(message, cause);
    }

    public EspressoException(Throwable cause) {
        super(cause);
    }

    public abstract HttpStatus.Code getStatus();

}
