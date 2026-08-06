package io.github.lazyboy81.espresso.core.engine;

import io.github.lazyboy81.espresso.core.handler.Request;
import io.github.lazyboy81.espresso.core.handler.Response;

public interface RequestProcessor<T, R> {

    Request processRequest(T request);
    Response processResponse(R response);

//    private void writeError(Response response, Callback callback, Exception e) {
//        Optional<String> clientAcceptType = Optional.ofNullable(response.getRequest().getHeaders().get(HttpHeader.ACCEPT));
//
//        if (e instanceof EspressoException ee) {
//            var binding = Bindings.find(clientAcceptType.orElse(""));
//
//            String errorPayload;
//            if (binding instanceof HtmlRender) {
//
//                errorPayload = binding.serialize(TemplateData.builder()
//                        .name("error")
//                        .vars(Map.of(
//                                "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
//                                "status", ee.getStatus().getCode(),
//                                "error", ee.getStatus().getMessage(),
//                                "message", ee.getMessage()
//                        ))
//                        .build());
//            } else {
//                errorPayload = binding.serialize(Map.of(
//                        "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
//                        "status", ee.getStatus().getCode(),
//                        "error", ee.getStatus().getMessage(),
//                        "message", ee.getMessage()
//                ));
//            }
//            response.setStatus(ee.getStatus().getCode());
//            response.getHeaders().put(HttpHeader.CONTENT_TYPE, binding.contentTypeValue());
//            response.write(true, ByteBuffer.wrap(errorPayload.getBytes(StandardCharsets.UTF_8)), callback);
//        } else {
//            String errorPayload = Bindings.json().serialize(Map.of(
//                    "timestamp", DateTimeUtil.rfc1123DateFormat(Instant.now()),
//                    "status", HttpStatus.Code.INTERNAL_SERVER_ERROR.getCode(),
//                    "error", HttpStatus.Code.INTERNAL_SERVER_ERROR.getMessage(),
//                    "message", e.getMessage()
//            ));
//            response.setStatus(HttpStatus.Code.INTERNAL_SERVER_ERROR.getCode());
//            response.getHeaders().put(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON_UTF_8.asString());
//            response.write(true, ByteBuffer.wrap(errorPayload.getBytes(StandardCharsets.UTF_8)), callback);
//        }
//    }

}
