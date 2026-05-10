package com.github.sinakarimi81.espresso.handler;


@FunctionalInterface
public interface Handler {

    void handle(Request request, Response response) throws Exception;

}
