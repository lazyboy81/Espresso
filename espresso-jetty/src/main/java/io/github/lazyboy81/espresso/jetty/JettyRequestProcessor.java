package io.github.lazyboy81.espresso.jetty;

import io.github.lazyboy81.espresso.core.engine.RequestProcessor;
import io.github.lazyboy81.espresso.core.http.FormValues;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.PathVariables;
import io.github.lazyboy81.espresso.core.http.Query;
import io.github.lazyboy81.espresso.core.http.constants.HttpHeader;
import io.github.lazyboy81.espresso.core.http.constants.MediaType;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.FormFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JettyRequestProcessor implements RequestProcessor<Request, Response> {

    private final Callback callback;

    @Override
    public io.github.lazyboy81.espresso.core.handler.Request processRequest(Request request) {
        try {
            var method = request.getMethod();
            var path = request.getHttpURI().getPath();

            var headers = getHeaders(request);

            var query = getQuery(request);

//            Tuple<PathVariables, Handler> pathVarsAndHandler = extractPathVariablesAndReturnHandler(method, path);

//            PathVariables pathVariables = pathVarsAndHandler.left();
            PathVariables pathVariables = new PathVariables(Map.of());

            FormValues formValues = checkHeaderAndReturnFormValues(headers, request);

            ByteBuffer byteBuffer = Content.Source.asByteBuffer(request);

            return new io.github.lazyboy81.espresso.core.handler.Request(
                    method,
                    path,
                    headers,
                    query,
                    pathVariables,
                    formValues,
                    byteBuffer.array()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Query getQuery(Request request) {
        var collectedQueryParams = Request.extractQueryParameters(request)
                .stream()
                .map(f -> Map.entry(f.getName(), f.getValue()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return Query.from(collectedQueryParams); // to make sure it cannot b;
    }

    private Headers getHeaders(Request request) {
        var collectedHeaders = request.getHeaders()
                .get()
                .stream()
                .map(f -> Map.entry(f.getName(), f.getValue()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return Headers.from(collectedHeaders); // to make sure it cannot be mutated later;
    }

    private FormValues checkHeaderAndReturnFormValues(Headers headers, Request request) {
        if (headers.hasValue(HttpHeader.CONTENT_TYPE.value(), MediaType.APPLICATION_FORM_URLENCODED.value())) {
            var collectedFormValues = FormFields.getFields(request)
                    .stream()
                    .map(f -> Map.entry(f.getName(), f.getValue()))
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
            return new FormValues(collectedFormValues); // to make sure it cannot be mutated later
        }

        return new FormValues(Map.of());
    }

    @Override
    public io.github.lazyboy81.espresso.core.handler.Response processResponse(Response response) {
        return new io.github.lazyboy81.espresso.core.handler.Response(new JettyResponseChannel(response, callback));
    }

}
