package io.github.lazyboy81.espresso.core.middleware;

import io.github.lazyboy81.espresso.core.handler.Handler;

public interface Middleware {

    Handler handle(Handler next);

}
