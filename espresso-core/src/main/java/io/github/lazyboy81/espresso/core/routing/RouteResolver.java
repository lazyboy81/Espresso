package io.github.lazyboy81.espresso.core.routing;

import io.github.lazyboy81.espresso.core.http.constants.HttpMethod;

public interface RouteResolver {

    RouteMatch resolve(HttpMethod method, String path);

}
