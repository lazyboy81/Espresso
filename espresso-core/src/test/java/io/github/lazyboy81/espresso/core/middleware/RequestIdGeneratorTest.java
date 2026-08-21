//package io.github.lazyboy81.espresso.core.middleware;
//
//import io.github.lazyboy81.espresso.core.handler.Handler;
//import io.github.lazyboy81.espresso.core.handler.Request;
//import io.github.lazyboy81.espresso.core.handler.Response;
//import io.github.lazyboy81.espresso.core.http.Headers;
//import io.github.lazyboy81.espresso.core.http.constants.HttpStatus;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.function.Supplier;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.fail;
//
//public class RequestIdGeneratorTest {
//
//    @Test
//    public void testRequestId() {
//        try {
//            var middleware = new RequestIdGenerator();
//
//            Handler endpoint = (request, response) -> response.text(HttpStatus.OK, "Test");
//
//            Handler decorated = middleware.handle(endpoint);
//
//            Request request = createTestRequest(false);
//            Response response = createTestResponse();
//
//            decorated.handle(request, response);
//            assertThat(response.headers().getHeaderValue("X-Request-ID")).isNotBlank();
//        } catch (Exception e) {
//            fail("test failed due to exception", e);
//        }
//    }
//
//    @Test
//    public void testRequestId_WhenHeaderExistsInRequest() {
//        try {
//            var middleware = new RequestIdGenerator();
//
//            Handler endpoint = (request, response) -> response.text(HttpStatus.OK, "Test");
//
//            Handler decorated = middleware.handle(endpoint);
//
//            Request request = createTestRequest(true);
//            Response response = createTestResponse();
//
//            decorated.handle(request, response);
//
//            assertThat(response.headers().getHeaderValue("X-Request-ID")).isEqualTo("<sample-request-id>");
//        } catch (Exception e) {
//            fail("test failed due to exception", e);
//        }
//    }
//
//    private Request createTestRequest(boolean sampleHeader) throws IOException {
//        Map<String, String> httpFields = new HashMap<>();
//        httpFields.put("X-Request-ID", "<sample-request-id>");
//        var header = new Headers(sampleHeader ? httpFields : Map.of());
//
//        return null;
//    }
//
//    private Response createTestResponse() {
//        return new Response(new MockResponse(), Callback.NOOP);
//    }
//
//    private record MockResponse(Ma httpFields) implements org.eclipse.jetty.server.Response {
//
//            @Override
//            public org.eclipse.jetty.server.Request getRequest() {
//                return null;
//            }
//
//            @Override
//            public int getStatus() {
//                return 0;
//            }
//
//            @Override
//            public void setStatus(int code) {
//
//            }
//
//            @Override
//            public HttpFields.Mutable getHeaders() {
//                return httpFields;
//            }
//
//            @Override
//            public Supplier<HttpFields> getTrailersSupplier() {
//                return null;
//            }
//
//            @Override
//            public void setTrailersSupplier(Supplier<HttpFields> trailers) {
//
//            }
//
//            @Override
//            public boolean isCommitted() {
//                return false;
//            }
//
//            @Override
//            public boolean hasLastWrite() {
//                return false;
//            }
//
//            @Override
//            public boolean isCompletedSuccessfully() {
//                return false;
//            }
//
//            @Override
//            public void reset() {
//
//            }
//
//            @Override
//            public CompletableFuture<Void> writeInterim(int status, HttpFields headers) {
//                return null;
//            }
//
//            @Override
//            public void write(boolean last, ByteBuffer byteBuffer, Callback callback) {
//
//            }
//        }
//
//}
