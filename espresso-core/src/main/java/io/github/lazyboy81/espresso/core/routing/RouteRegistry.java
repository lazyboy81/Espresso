package io.github.lazyboy81.espresso.core.routing;

import io.github.lazyboy81.espresso.core.exception.PathNotFoundException;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.http.constants.HttpMethod;
import io.github.lazyboy81.espresso.core.middleware.Middleware;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RouteRegistry {

    private static final Pattern PATH_VAR_NO_NAME_PATTERN = Pattern.compile(":(?![A-Za-z0-9]+)");
    private static final Pattern PATH_VAR_MULTI_CAPTURE_PATTERN = Pattern.compile(":([A-Za-z0-9_-]+)(?=.*:\\1(?=/|$))");

    private final EnumMap<HttpMethod, MethodRouteTable> routesByMethod;
    private final List<Middleware> globalMiddlewares;

    public RouteRegistry() {
        this.routesByMethod = new EnumMap<>(HttpMethod.class);
        this.globalMiddlewares = new ArrayList<>();
    }

    public void options(String path, Handler handler) {
        addRoute(HttpMethod.OPTIONS, path, handler, globalMiddlewares);
    }

    public void head(String path, Handler handler) {
        addRoute(HttpMethod.HEAD, path, handler, globalMiddlewares);
    }

    public void get(String path, Handler handler) {
        addRoute(HttpMethod.GET, path, handler, globalMiddlewares);
    }

    public void post(String path, Handler handler) {
        addRoute(HttpMethod.POST, path, handler, globalMiddlewares);
    }

    public void put(String path, Handler handler) {
        addRoute(HttpMethod.PUT, path, handler, globalMiddlewares);
    }

    public void patch(String path, Handler handler) {
        addRoute(HttpMethod.PATCH, path, handler, globalMiddlewares);
    }

    public void trace(String path, Handler handler) {
        addRoute(HttpMethod.TRACE, path, handler, globalMiddlewares);
    }

    public void delete(String path, Handler handler) {
        addRoute(HttpMethod.DELETE, path, handler, globalMiddlewares);
    }

    public void any(String path, Handler handler) {
        for (HttpMethod method : HttpMethod.methods) {
            addRoute(method, path, handler, globalMiddlewares);
        }
    }

    public RouteGroup group(String root) {
        if (root == null) {
            throw new IllegalArgumentException("root cannot be null");
        }

        return new RouteGroup(addStartingSlash(root), this, globalMiddlewares);
    }

    private String addStartingSlash(String root) {
        if (!root.startsWith("/") || root.isBlank()) {
            return  "/".concat(root);
        }

        return root;
    }

    public void use(Middleware middleware) {
        globalMiddlewares.add(middleware);
    }

    void addRoute(HttpMethod method, String path, Handler handler, List<Middleware> middlewares) {
        if (handler == null) {
            throw new IllegalArgumentException("handler cannot be null");
        }

        validateInputPath(path);

        path = normalizePath(path);

        MethodRouteTable routerTable = routesByMethod.computeIfAbsent(method, m -> new MethodRouteTable());
        routerTable.addPath(path, applyMiddlewares(handler, middlewares));
    }

    private void validateInputPath(String path) {
        // if the input is null
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }

        // if the input contains a path variable without a name
        if (PATH_VAR_NO_NAME_PATTERN.matcher(path).find()) {
            throw new IllegalArgumentException("the given path does not capture the variable: " + path);
        }

        // if the input contains a duplicate path variable name
        Matcher matcher = PATH_VAR_MULTI_CAPTURE_PATTERN.matcher(path);
        if (matcher.find()) {
            String duplicatedPathVariable = matcher.group();
            throw new IllegalArgumentException(String.format("Not allowed to capture '%s' more than once in the same pattern: %s", duplicatedPathVariable, path));
        }
    }

    private static String normalizePath(String path) {
        // do prevent problems down the line
        // or this means the client is setting a handler for the '/' path
        if (!path.startsWith("/") || path.isBlank()) {
            path = "/".concat(path);
        }

        while (path.contains("//")) {
            path = path.replaceAll("//", "/");
        }

        while (!path.equals("/") && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    private Handler applyMiddlewares(Handler original, List<Middleware> middlewares) {
        var result = original;

        for (Middleware middleware : middlewares.reversed()) {
            result = middleware.handle(result);
        }

        return result;
    }

    public RouteMatch resolve(HttpMethod method, String path) {
        MethodRouteTable table = routesByMethod.get(method);
        if (table == null) {
            throw new PathNotFoundException(String.format("Path '%s %s' was not found", method.asString(), path));
        }

        return table.match(path)
                .orElseThrow(() -> new PathNotFoundException(String.format("Path '%s %s' was not found", method.asString(), path)));
    }

}
