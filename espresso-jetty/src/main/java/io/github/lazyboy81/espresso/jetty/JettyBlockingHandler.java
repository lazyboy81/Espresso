package io.github.lazyboy81.espresso.jetty;

import io.github.lazyboy81.espresso.core.binding.Bindings;
import io.github.lazyboy81.espresso.core.binding.dto.TemplateData;
import io.github.lazyboy81.espresso.core.binding.impl.HtmlRender;
import io.github.lazyboy81.espresso.core.exception.EspressoException;
import io.github.lazyboy81.espresso.core.handler.RequestUtil;
import io.github.lazyboy81.espresso.core.http.constants.HttpMethod;
import io.github.lazyboy81.espresso.core.routing.RouteMatch;
import io.github.lazyboy81.espresso.core.routing.RouteResolver;
import io.github.lazyboy81.espresso.core.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class JettyBlockingHandler extends Handler.Abstract {

    private final JettyRequestProcessor processor;
    private final RouteResolver routeResolver;

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        try {
            var req = processor.processRequest(request);
            var res = processor.processResponse(response, callback);

            RouteMatch resolve = routeResolver.resolve(HttpMethod.fromValue(req.method()), req.path());

            req = RequestUtil.toBuilder(req).pathVariables(resolve.pathVariables()).build();

            resolve.handler().handle(req, res);
        } catch (Exception e) {
            writeError(response, callback, e);
        }

        return true;
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
                                "status", ee.getStatus().value(),
                                "error", ee.getStatus().getReasonPhrase(),
                                "message", ee.getMessage()
                        ))
                        .build());
            } else {
                errorPayload = binding.serialize(Map.of(
                        "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
                        "status", ee.getStatus().value(),
                        "error", ee.getStatus().getReasonPhrase(),
                        "message", ee.getMessage()
                ));
            }
            response.setStatus(ee.getStatus().value());
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
