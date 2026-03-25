package com.github.sinakarimi81.espresso.context;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.http.PathVariables;
import com.github.sinakarimi81.espresso.http.Query;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Request {

    private Headers headers;
    private PathVariables pathVariables;
    private Query query;
    private String payload;

    /**
     * <p>contains the request header fields either received
     * by the server or to be sent by the client.</p>
     * @return headers sent by client. see {@link Headers}
     */
    public Headers headers() {
        return headers;
    }

    /**
     * <p>contains the request path variables (or params).</p>
     * <p>for example for url "/user/:id" we would have</p>
     * <p>{@code GET /user/1234/}</p>
     * <p>{@code pathVariables().get("id") == "1234"}</p>
     * @return path variables sent by client. see {@link PathVariables}
     */
    public PathVariables pathVariables() {
        return pathVariables;
    }

    /**
     * <p>contains the request path variables (or params).</p>
     * <p>for example for url "/path" we would have</p>
     * <p>{@code GET /path?id=1234&name=Manu&value=}</p>
     * <p>{@code query().get("id") == "1234"}</p>
     * <p>{@code query().get("name") == "Manu"}</p>
     * <p>{@code query().get("value") == ""}</p>
     * <p>{@code query().get("wtf") == ""}}</p>
     * @return query parameters sent by client. see {@link Query}
     */
    public Query query() {
        return query;
    }

    public <T> T json(Class<T> targetType) {
        return Bindings.json().bind(payload, targetType);
    }

}
