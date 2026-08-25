package io.github.lazyboy81.espresso.jetty;

import io.github.lazyboy81.espresso.core.http.FormValues;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.Query;
import io.github.lazyboy81.espresso.core.http.constants.HttpHeader;
import io.github.lazyboy81.espresso.core.http.constants.MediaType;
import lombok.RequiredArgsConstructor;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.FormFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class JettyRequestProcessor {

    public io.github.lazyboy81.espresso.core.handler.Request processRequest(Request request) {
        try {
            var requestBuilder = io.github.lazyboy81.espresso.core.handler.Request.RequestBuilder.newBuilder();

            requestBuilder.method(request.getMethod());
            requestBuilder.path(request.getHttpURI().getPath());

            Headers headers = getHeaders(request);
            requestBuilder.headers(headers);
            requestBuilder.formValues(checkHeaderAndReturnFormValues(headers, request));
            requestBuilder.query(getQuery(request));

            ByteBuffer payloadBuffer = Content.Source.asByteBuffer(request);
            byte[] payload = extractPayload(headers, payloadBuffer);
            requestBuilder.payload(payload);

            return requestBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Query getQuery(Request request) {
        var stream = Request.extractQueryParameters(request)
                .stream()
                .map(f -> Map.entry(f.getName(), f.getValues()));

        var collectedQueryParams = convertEntriesToMap(stream);

        return Query.from(collectedQueryParams); // to make sure it cannot b;
    }

    private Headers getHeaders(Request request) {
        var stream = request.getHeaders()
                .get()
                .stream()
                .map(f -> Map.entry(f.getName(), f.getValueList()));

        var collectedHeaders = convertEntriesToMap(stream);

        return Headers.from(collectedHeaders); // to make sure it cannot be mutated later;
    }

    private FormValues checkHeaderAndReturnFormValues(Headers headers, Request request) {
        if (!headers.hasValue(HttpHeader.CONTENT_TYPE.value(), MediaType.APPLICATION_FORM_URLENCODED.value())) {
            return new FormValues(Map.of());
        }

        Fields fields = FormFields.getFields(request);
        var stream = fields
                .stream()
                .map(f -> Map.entry(f.getName(), f.getValues()));

        var collectedFormValues = convertEntriesToMap(stream);

        return new FormValues(collectedFormValues); // to make sure it cannot be mutated later
    }

    private Map<String, String> convertEntriesToMap(Stream<Map.Entry<String, List<String>>> entries) {
        return entries.map(this::splitEntries)
                .flatMap(List::stream)
                .collect(
                        Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (existingName, newName) -> existingName + "," + newName
                        )
                );
    }

    private List<Map.Entry<String, String>> splitEntries(Map.Entry<String, List<String>> input) {
        String key = input.getKey();
        return input.getValue().stream()
                .map(s -> Map.entry(key, s))
                .toList();
    }

    private byte[] extractPayload(Headers headers, ByteBuffer payloadBuffer) {
        var contentLength = headers.containsHeader(HttpHeader.CONTENT_LENGTH.value())
                ? Integer.parseInt(headers.getHeaderValue(HttpHeader.CONTENT_LENGTH.value()))
                : 0;

        var dataLength = Math.min(contentLength, payloadBuffer.remaining());

        var result = new byte[dataLength];

        payloadBuffer.get(result, 0, dataLength);

        return result;
    }

    public io.github.lazyboy81.espresso.core.handler.Response processResponse(Response response, Callback callback) {
        return new io.github.lazyboy81.espresso.core.handler.Response(new JettyResponseChannel(response, callback));
    }

}
