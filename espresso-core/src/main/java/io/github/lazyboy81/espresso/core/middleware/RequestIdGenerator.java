package io.github.lazyboy81.espresso.core.middleware;

import io.github.lazyboy81.espresso.core.handler.Handler;

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
