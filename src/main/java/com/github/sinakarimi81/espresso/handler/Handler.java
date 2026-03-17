package com.github.sinakarimi81.espresso.handler;

import com.github.sinakarimi81.espresso.context.Context;

@FunctionalInterface
public interface Handler {

    void handle(Context context) throws Exception;

}
