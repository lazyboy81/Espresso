package io.github.lazyboy81.espresso.core.middleware;

import io.github.lazyboy81.espresso.core.engine.ResponseChannel;
import io.github.lazyboy81.espresso.core.handler.Handler;
import io.github.lazyboy81.espresso.core.handler.Request;
import io.github.lazyboy81.espresso.core.handler.Response;
import io.github.lazyboy81.espresso.core.http.Headers;
import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class RequestIdGeneratorTest {

    @Test
    public void testRequestId() {
        var middleware = new RequestIdGenerator();

        Handler endpoint = (request, response) -> response.text(HttpStatus.OK, "Test");

        Handler decorated = middleware.handle(endpoint);

        Request request = createTestRequest(false);
        Response response = createTestResponse();

        assertThatNoException().isThrownBy(() -> decorated.handle(request, response));
        assertThat(response.headers().getHeaderValue("X-Request-ID")).isNotBlank();
    }

    @Test
    public void testRequestId_WhenHeaderExistsInRequest() {
        var middleware = new RequestIdGenerator();

        Handler endpoint = (request, response) -> response.text(HttpStatus.OK, "Test");

        Handler decorated = middleware.handle(endpoint);

        Request request = createTestRequest(true);
        Response response = createTestResponse();

        assertThatNoException().isThrownBy(() -> decorated.handle(request, response));

        assertThat(response.headers().getHeaderValue("X-Request-ID")).isEqualTo("<sample-request-id>");
    }

    private Request createTestRequest(boolean sampleHeader) {
        Map<String, String> httpFields = new HashMap<>();
        httpFields.put("X-Request-ID", "<sample-request-id>");
        var header = new Headers(sampleHeader ? httpFields : Map.of());

        return Request.RequestBuilder.newBuilder()
                .headers(header)
                .build();
    }

    private Response createTestResponse() {
        return new Response(new MockResponseChannel(new HashMap<>()));
    }

    private record MockResponseChannel(Map<String, String> map) implements ResponseChannel {

        @Override
        public int status() {
            return 0;
        }

        @Override
        public void status(int status) {

        }

        @Override
        public void setHeader(String name, String value) {
            map.put(name, value);
        }

        @Override
        public void removeHeader(String name) {

        }

        @Override
        public Headers getHeaders() {
            return new Headers(map);
        }

        @Override
        public void write(byte[] body) {

        }

        @Override
        public boolean committed() {
            return false;
        }

        @Override
        public void fail(Throwable failure) {

        }

        @Override
        public byte[] capturedPayload() {
            return new byte[0];
        }
    }
}
