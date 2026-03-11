package com.github.sinakarimi81.espresso.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class Context {

    private Request request;
    private Response response;

    public Request request() {
        return request;
    }

    public Response response() {
        return response;
    }

    public <T> T jsonPayload(Class<T> type) {
        return null;
    }

}
