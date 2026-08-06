package io.github.lazyboy81.espresso.core.exception;

import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;

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

    public abstract HttpStatus getStatus();

}
