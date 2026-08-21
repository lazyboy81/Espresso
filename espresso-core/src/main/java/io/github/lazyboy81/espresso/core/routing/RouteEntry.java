package io.github.lazyboy81.espresso.core.routing;

import io.github.lazyboy81.espresso.core.handler.Handler;

record RouteEntry(RoutePattern pattern,
                  Handler handler) {}
