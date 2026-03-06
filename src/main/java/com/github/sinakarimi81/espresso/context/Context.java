package com.github.sinakarimi81.espresso.context;

import com.github.sinakarimi81.espresso.engine.Engine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Context {

    private Engine engine;
    private Request request;
    private Response response;

    public <T> T jsonPayload(Class<T> type) {
        return null;
    }

}
