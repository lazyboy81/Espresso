package com.github.sinakarimi81.espresso.context;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.http.Headers;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Request {

    private Headers headers;
    private String payload;

    /**
     * <p>contains the request header fields either received
     * by the server or to be sent by the client.</p>
     * @return headers sent by client. see {@link Headers}
     */
    public Headers headers() {
        return headers;
    }

    public <T> T json(Class<T> targetType) {
        return Bindings.json().bind(payload, targetType);
    }

}
