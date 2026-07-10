package com.github.sinakarimi81.espresso.middleware;

import com.github.sinakarimi81.espresso.handler.Handler;

public interface Middleware {

    Handler handle(Handler next);

}
