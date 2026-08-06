package io.github.lazyboy81.espresso.core.handler;


@FunctionalInterface
public interface Handler {

    void handle(Request request, Response response) throws Exception;

}
