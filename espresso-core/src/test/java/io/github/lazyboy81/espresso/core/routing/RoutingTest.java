package io.github.lazyboy81.espresso.core.routing;

import io.github.lazyboy81.espresso.core.exception.AmbiguousPathException;
import io.github.lazyboy81.espresso.core.exception.PathNotFoundException;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.http.constants.HttpMethod;
import io.github.lazyboy81.espresso.core.middleware.Middleware;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class RoutingTest {

    @Test
    void registersRootAndNestedLiteralRoutes() {
        RouteRegistry routes = new RouteRegistry();
        Handler root = handler("root");
        Handler nested = handler("nested");

        routes.get("/", root);
        routes.get("/users/settings", nested);

        assertResolved(routes, HttpMethod.GET, "/", root, Map.of());
        assertResolved(routes, HttpMethod.GET, "/users/settings", nested, Map.of());
    }

    @Test
    void registersEveryExplicitHttpMethodInItsOwnTable() {
        RouteRegistry routes = new RouteRegistry();
        Handler options = handler("options");
        Handler head = handler("head");
        Handler get = handler("get");
        Handler post = handler("post");
        Handler put = handler("put");
        Handler patch = handler("patch");
        Handler trace = handler("trace");
        Handler delete = handler("delete");

        routes.options("/options", options);
        routes.head("/head", head);
        routes.get("/get", get);
        routes.post("/post", post);
        routes.put("/put", put);
        routes.patch("/patch", patch);
        routes.trace("/trace", trace);
        routes.delete("/delete", delete);

        assertResolved(routes, HttpMethod.OPTIONS, "/options", options, Map.of());
        assertResolved(routes, HttpMethod.HEAD, "/head", head, Map.of());
        assertResolved(routes, HttpMethod.GET, "/get", get, Map.of());
        assertResolved(routes, HttpMethod.POST, "/post", post, Map.of());
        assertResolved(routes, HttpMethod.PUT, "/put", put, Map.of());
        assertResolved(routes, HttpMethod.PATCH, "/patch", patch, Map.of());
        assertResolved(routes, HttpMethod.TRACE, "/trace", trace, Map.of());
        assertResolved(routes, HttpMethod.DELETE, "/delete", delete, Map.of());
    }

    @Test
    void anyRegistersTheSameHandlerForEveryDeclaredMethod() {
        RouteRegistry routes = new RouteRegistry();
        Handler handler = handler("any");

        routes.any("/health", handler);

        for (HttpMethod method : HttpMethod.methods) {
            assertResolved(routes, method, "/health", handler, Map.of());
        }
    }

    @Test
    void keepsRoutesIsolatedByHttpMethod() {
        RouteRegistry routes = new RouteRegistry();
        Handler get = handler("get");
        Handler post = handler("post");

        routes.get("/users", get);
        routes.post("/users", post);

        assertResolved(routes, HttpMethod.GET, "/users", get, Map.of());
        assertResolved(routes, HttpMethod.POST, "/users", post, Map.of());
        assertThatThrownBy(() -> routes.resolve(HttpMethod.DELETE, "/users"))
                .isInstanceOf(PathNotFoundException.class);
    }

    @Test
    void rejectsInvalidAndNonCanonicalRoutePaths() {
        RouteRegistry routes = new RouteRegistry();
        Handler handler = handler("handler");

        assertThatThrownBy(() -> routes.get(null, handler)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> routes.get("/users", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> routes.get("/users/:", handler)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> routes.get("/users/:id/:id", handler)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDuplicateLiteralAndStructurallyEquivalentPatternRoutesPerMethod() {
        RouteRegistry routes = new RouteRegistry();

        routes.get("/accounts/:id", handler("first-parameter"));
        assertThatThrownBy(() -> routes.get("/accounts/:name", handler("second-parameter")))
                .isInstanceOf(AmbiguousPathException.class);
    }

    @Test
    void permitsEquivalentPathsForDifferentMethods() {
        RouteRegistry routes = new RouteRegistry();
        Handler get = handler("get");
        Handler post = handler("post");

        routes.get("/users/:id", get);
        routes.post("/users/:name", post);

        assertResolved(routes, HttpMethod.GET, "/users/42", get, Map.of("id", "42"));
        assertResolved(routes, HttpMethod.POST, "/users/42", post, Map.of("name", "42"));
    }

    @Test
    void normalizesTrailingSlashes() {
        RouteRegistry routes = new RouteRegistry();
        Handler handler = handler("health");

        routes.get("/health/", handler);

        assertResolved(routes, HttpMethod.GET, "/health", handler, Map.of());
    }

    @Test
    void normalizesRepeatedInternalSlashes() {
        RouteRegistry routes = new RouteRegistry();
        Handler handler = handler("health");

        routes.get("/health//", handler);

        assertResolved(routes, HttpMethod.GET, "/health", handler, Map.of());
    }

    @Test
    void routeGroupsApplyPrefixesAndSupportNestedGroups() {
        RouteRegistry routes = new RouteRegistry();
        Handler endpoint = handler("endpoint");

        RouteGroup api = routes.group("/api");
        RouteGroup v1 = api.group("/v1");
        v1.get("/users", endpoint);

        assertResolved(routes, HttpMethod.GET, "/api/v1/users", endpoint, Map.of());
    }

    @Test
    void routeGroupsRequireCanonicalPrefixesAndChildPaths() {
        RouteRegistry routes = new RouteRegistry();

        assertThatThrownBy(() -> routes.group(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void routeGroupsNormalizedInput() {
        RouteRegistry routes = new RouteRegistry();

        assertThatNoException().isThrownBy(() -> routes.group(""));
        assertThatNoException().isThrownBy(() -> routes.group("api"));
    }

    @Test
    void routeGroupAnyRegistersThePrefixedPathForEveryMethod() {
        RouteRegistry routes = new RouteRegistry();
        Handler handler = handler("group-any");

        routes.group("/api").any("/health", handler);

        for (HttpMethod method : HttpMethod.methods) {
            assertResolved(routes, method, "/api/health", handler, Map.of());
        }
    }

    @Test
    void globalMiddlewareOnlyAffectsRoutesRegisteredAfterItIsAdded() throws Exception {
        RouteRegistry routes = new RouteRegistry();
        List<String> calls = new ArrayList<>();
        Handler before = recordingHandler("before", calls);
        Handler after = recordingHandler("after", calls);

        routes.get("/before", before);
        routes.use(recordingMiddleware("global", calls));
        routes.get("/after", after);

        routes.resolve(HttpMethod.GET, "/before").handler().handle(null, null);
        assertThat(calls).containsExactly("before");

        calls.clear();
        routes.resolve(HttpMethod.GET, "/after").handler().handle(null, null);
        assertThat(calls).containsExactly("global", "after");
    }

    @Test
    void inheritedGlobalAndGroupMiddlewareExecuteInDeclarationOrder() throws Exception {
        RouteRegistry routes = new RouteRegistry();
        List<String> calls = new ArrayList<>();

        routes.use(recordingMiddleware("global", calls));
        RouteGroup api = routes.group("/api");
        api.use(recordingMiddleware("api", calls));
        RouteGroup v1 = api.group("/v1");
        v1.use(recordingMiddleware("v1", calls));
        v1.get("/users", recordingHandler("handler", calls));

        routes.resolve(HttpMethod.GET, "/api/v1/users").handler().handle(null, null);

        assertThat(calls).containsExactly("global", "api", "v1", "handler");
    }

    @Test
    void routePatternMatchesLiteralsAndExtractsMultipleNamedParameters() {
        RoutePattern pattern = new RoutePattern(
                "/users/:id/orders/:orderId",
                List.of(
                        new LiteralSegment("users"),
                        new ParameterSegment("id"),
                        new LiteralSegment("orders"),
                        new ParameterSegment("orderId")
                )
        );

        PatternMatch match = pattern.match(List.of("users", "42", "orders", "100")).orElseThrow();

        assertThat(match.pathParameters()).isEqualTo(Map.of("id", "42", "orderId", "100"));
        assertThatThrownBy(() -> match.pathParameters().put("other", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void routePatternRequiresTheWholePathToMatch() {
        RoutePattern pattern = new RoutePattern(
                "/users/:id",
                List.of(new LiteralSegment("users"), new ParameterSegment("id"))
        );

        assertThat(pattern.match(List.of("users"))).isEmpty();
        assertThat(pattern.match(List.of("users", "42", "orders"))).isEmpty();
        assertThat(pattern.match(List.of("accounts", "42"))).isEmpty();
    }

    @Test
    void catchAllCapturesAllRemainingSegmentsOrAnEmptyValue() {
        RouteRegistry routes = new RouteRegistry();
        Handler handler = handler("files");

        routes.get("/files/*/test", handler);

        assertResolved(routes, HttpMethod.GET, "/files/images/test", handler, Map.of());
        assertResolved(routes, HttpMethod.GET, "/files/movies/test", handler, Map.of());
    }

    @Test
    void exactRoutesWinOverPatternRoutesRegardlessOfRegistrationOrder() {
        assertExactRouteWins(true);
        assertExactRouteWins(false);
    }

    @Test
    void moreSpecificPatternWinsRegardlessOfRegistrationOrder() {
        assertMoreSpecificPatternWins(true);
        assertMoreSpecificPatternWins(false);
    }

    @Test
    void parametersWinOverCatchAllRoutesRegardlessOfRegistrationOrder() {
        assertParameterRouteWinsOverCatchAll(true);
        assertParameterRouteWinsOverCatchAll(false);
    }

    @Test
    void failedPatternCandidatesDoNotPreventLaterCompleteMatches() {
        RouteRegistry routes = new RouteRegistry();
        Handler profile = handler("profile");
        Handler newUser = handler("new-user");

        routes.get("/users/:id/profile", profile);
        routes.get("/users/new/:tab", newUser);

        assertThatThrownBy(() -> routes.resolve(HttpMethod.GET, "/users/42/settings"))
                .isInstanceOf(PathNotFoundException.class);
        assertResolved(routes, HttpMethod.GET, "/users/new/settings", newUser, Map.of("tab", "settings"));
    }

    private static void assertExactRouteWins(boolean patternRegisteredFirst) {
        RouteRegistry routes = new RouteRegistry();
        Handler exact = handler("exact");
        Handler pattern = handler("pattern");

        if (patternRegisteredFirst) {
            routes.get("/users/:id", pattern);
            routes.get("/users/settings", exact);
        } else {
            routes.get("/users/settings", exact);
            routes.get("/users/:id", pattern);
        }

        assertResolved(routes, HttpMethod.GET, "/users/settings", exact, Map.of());
    }

    private static void assertMoreSpecificPatternWins(boolean broadRouteRegisteredFirst) {
        RouteRegistry routes = new RouteRegistry();
        Handler broad = handler("broad");
        Handler specific = handler("specific");

        if (broadRouteRegisteredFirst) {
            routes.get("/users/:id/profile", broad);
            routes.get("/users/new/:tab", specific);
        } else {
            routes.get("/users/new/:tab", specific);
            routes.get("/users/:id/profile", broad);
        }

        assertResolved(routes, HttpMethod.GET, "/users/new/profile", specific, Map.of("tab", "profile"));
    }

    private static void assertParameterRouteWinsOverCatchAll(boolean catchAllRegisteredFirst) {
        RouteRegistry routes = new RouteRegistry();
        Handler parameter = handler("parameter");
        Handler catchAll = handler("catch-all");

        if (catchAllRegisteredFirst) {
            routes.get("/assets/*path", catchAll);
            routes.get("/assets/:type/:file", parameter);
        } else {
            routes.get("/assets/:type/:file", parameter);
            routes.get("/assets/*path", catchAll);
        }

        assertResolved(routes, HttpMethod.GET, "/assets/images/logo.png", parameter,
                Map.of("type", "images", "file", "logo.png"));
    }

    private static void assertResolved(RouteRegistry routes,
                                       HttpMethod method,
                                       String path, Handler expectedHandler,
                                       Map<String, String> expectedPathVariables) {
        RouteMatch match = routes.resolve(method, path);

        assertThat(match.handler()).isSameAs(expectedHandler);
        assertThat(match.pathVariables().params()).isEqualTo(expectedPathVariables);
    }

    private static Handler handler(String name) {
        return new NamedHandler(name);
    }

    private static Handler recordingHandler(String name, List<String> calls) {
        return (request, response) -> calls.add(name);
    }

    private static Middleware recordingMiddleware(String name, List<String> calls) {
        return next -> (request, response) -> {
            calls.add(name);
            next.handle(request, response);
        };
    }

    private record NamedHandler(String name) implements Handler {
        @Override
        public void handle(io.github.lazyboy81.espresso.core.handler.Request request,
                           io.github.lazyboy81.espresso.core.handler.Response response) {
            // Deliberately empty: instance identity represents the registered endpoint.
        }
    }
}
