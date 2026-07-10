package com.github.sinakarimi81.espresso.middleware;

import com.github.sinakarimi81.espresso.handler.Handler;

import java.util.UUID;

class RequestIdGenerator implements Middleware {

    @Override
    public Handler handle(Handler next) {
        return (request, response) -> {
            String headerValue = request.headers().getHeaderValue("X-Request-Id");
            if (headerValue == null || headerValue.isBlank()) {
                headerValue = UUID.randomUUID().toString();
            }

            response.header("X-Request-Id", headerValue);

            next.handle(request, response);
        };
    }

}
