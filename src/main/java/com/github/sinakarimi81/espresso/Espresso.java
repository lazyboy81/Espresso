package com.github.sinakarimi81.espresso;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.binding.Serialization;
import com.github.sinakarimi81.espresso.binding.dto.TemplateData;
import com.github.sinakarimi81.espresso.binding.impl.HtmlRender;
import com.github.sinakarimi81.espresso.exception.EspressoException;
import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.http.FormValues;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.http.PathVariables;
import com.github.sinakarimi81.espresso.http.Query;
import com.github.sinakarimi81.espresso.routing.Router;
import com.github.sinakarimi81.espresso.routing.Routes;
import com.github.sinakarimi81.espresso.util.DateTimeUtil;
import com.github.sinakarimi81.espresso.util.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpHeaderValue;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Espresso {

    private static Espresso INSTANCE = null;

    private static final int DEFAULT_PORT = 8080;
    private final ExecutorService executorService = Executors.newFixedThreadPool(60);
    private final Routes routes;
    private final Server server;

    public static Espresso getDefault() {
        try {
            if (INSTANCE == null) {
                INSTANCE = new Espresso(DEFAULT_PORT);
            }
            return INSTANCE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Espresso withPort(int port) {
        try {
            if (INSTANCE == null) {
                INSTANCE = new Espresso(port);
            }
            return INSTANCE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Espresso(int port) {
        this.routes = new Routes();
        server = new Server(port);
    }

    public Router group(String root) {
        return routes.group(root);
    }

    public void options(String path, Handler handler) {
        routes.options(path, handler);
    }

    public void head(String path, Handler handler) {
        routes.head(path, handler);
    }

    public void get(String path, Handler handler) {
        routes.get(path, handler);
    }

    public void post(String path, Handler handler) {
        routes.post(path, handler);
    }

    public void put(String path, Handler handler) {
        routes.put(path, handler);
    }

    public void delete(String path, Handler handler) {
        routes.delete(path, handler);
    }

    public void any(String path, Handler handler) {
        routes.any(path, handler);
    }

    public void start() {

        // this is blocking
        server.setHandler(new org.eclipse.jetty.server.Handler.Abstract() {

            @Override
            public boolean handle(Request request, Response response, Callback callback) {
//                CompletableFuture.runAsync(() -> , executorService);
                process(request, response, callback);
                return true;
            }

        });

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error("failed to start the server", e);
            throw new RuntimeException(e);
        }
    }

    private void process(Request request, Response response, Callback callback) {
        try {
            var method = request.getMethod();
            var path = request.getHttpURI().getPath();
            var version = request.getConnectionMetaData().getHttpVersion();

            var headers = Headers.from(request.getHeaders().asImmutable()); // to make sure it cannot be mutated later
            var query = Query.from(Request.extractQueryParameters(request).asImmutable()); // to make sure it cannot be mutated later

            Tuple<PathVariables, Handler> pathVarsAndHandler = extractPathVariablesAndReturnHandler(method, path);

            PathVariables pathVariables = pathVarsAndHandler.left();

            FormValues formValues = checkHeaderAndReturnFormValues(headers, request);

            InputStream content = Content.Source.asInputStream(request);

            var req = new com.github.sinakarimi81.espresso.handler.Request(
                    headers,
                    query,
                    pathVariables,
                    formValues,
                    content
            );
            var resp = new com.github.sinakarimi81.espresso.handler.Response(
                    response,
                    callback
            );


            Handler handler = pathVarsAndHandler.right();
            handler.handle(req, resp);
        } catch (Exception e) {
            writeError(response, callback, e);
        }
    }

    private FormValues checkHeaderAndReturnFormValues(Headers headers, Request request) {
        if (headers.hasValue(HttpHeader.CONTENT_TYPE, MimeTypes.Type.FORM_ENCODED.asString()) ||
                headers.hasValue(HttpHeader.CONTENT_TYPE, MimeTypes.Type.FORM_ENCODED_UTF_8.asString()) ||
                headers.hasValue(HttpHeader.CONTENT_TYPE, MimeTypes.Type.FORM_ENCODED_8859_1.asString())) {
            return new FormValues(FormFields.getFields(request).asImmutable()); // to make sure it cannot be mutated later
        }

        return new FormValues(Fields.EMPTY);
    }

    private Tuple<PathVariables, Handler> extractPathVariablesAndReturnHandler(String method, String urlPath) {
        var pathVars = new HashMap<String, String>();

        Handler handlerForPath = routes.getHandlerForPath(method, urlPath, pathVars);

        return Tuple.of(new PathVariables(pathVars), handlerForPath);
    }

    private void writeError(Response response, Callback callback, Exception e) {
        Optional<String> clientAcceptType = Optional.ofNullable(response.getRequest().getHeaders().get(HttpHeader.ACCEPT));

        if (e instanceof EspressoException ee) {
            var binding = Bindings.find(clientAcceptType.orElse(""));

            String errorPayload;
            if (binding instanceof HtmlRender) {

                errorPayload = binding.serialize(TemplateData.builder()
                        .name("error")
                        .vars(Map.of(
                                "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                                "status", ee.getStatus().getCode(),
                                "error", ee.getStatus().getMessage(),
                                "message", ee.getMessage()
                        ))
                        .build());
            } else {
                errorPayload = binding.serialize(Map.of(
                        "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                        "status", ee.getStatus().getCode(),
                        "error", ee.getStatus().getMessage(),
                        "message", ee.getMessage()
                ));
            }
            response.setStatus(ee.getStatus().getCode());
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, binding.contentTypeValue());
            response.write(true, ByteBuffer.wrap(errorPayload.getBytes(StandardCharsets.UTF_8)), callback);
        } else {
            String errorPayload = Bindings.json().serialize(Map.of(
                    "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                    "status", HttpStatus.Code.INTERNAL_SERVER_ERROR.getCode(),
                    "error", HttpStatus.Code.INTERNAL_SERVER_ERROR.getMessage(),
                    "message", e.getMessage()
            ));
            response.setStatus(HttpStatus.Code.INTERNAL_SERVER_ERROR.getCode());
            response.getHeaders().put(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON_UTF_8.asString());
            response.write(true, ByteBuffer.wrap(errorPayload.getBytes(StandardCharsets.UTF_8)), callback);
        }
    }
}
