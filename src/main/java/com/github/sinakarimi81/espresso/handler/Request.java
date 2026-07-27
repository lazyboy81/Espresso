package com.github.sinakarimi81.espresso.handler;

import com.github.sinakarimi81.espresso.binding.Bindings;
import com.github.sinakarimi81.espresso.http.FormValues;
import com.github.sinakarimi81.espresso.http.Headers;
import com.github.sinakarimi81.espresso.http.PathVariables;
import com.github.sinakarimi81.espresso.http.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Request {

    private final String method;
    private final String path;
    private final Headers headers;
    private final Query query;
    private final PathVariables pathVariables;
    private final FormValues formValues;
    // TODO: we can later switch to ByteBuffer and off-heap memory if needed
    private final byte[] payload;

    public Request(String method, String path, Headers headers, Query query, PathVariables pathVariables, FormValues formValues, InputStream payload) throws IOException {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.query = query;
        this.pathVariables = pathVariables;
        this.formValues = formValues;
        this.payload = payload.readAllBytes();
    }

    /**
     * @return the request method
     */
    public String method() {
        return method;
    }

    /**
     * @return the request path
     */
    public String path() {
        return path;
    }

    /**
     * <p>contains the request header fields either received
     * by the server or to be sent by the client.</p>
     *
     * @return value sent by client. see {@link Headers}
     */
    public Headers headers() {
        return headers;
    }

    /**
     * <p>contains the request path variables (or params).</p>
     * <p>for example for url "/user/:id" we would have</p>
     * <p>{@code GET /user/1234/}</p>
     * <p>{@code pathVariables().get("id") == "1234"}</p>
     *
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
     *
     * @return query parameters sent by client. see {@link Query}
     */
    public Query query() {
        return query;
    }

    /**
     * @return the form values of the request
     * @implNote if the request's content-type header does not contain the following values
     * <ul>
     *     <li> {@link org.eclipse.jetty.http.MimeTypes.Type#FORM_ENCODED} </li>
     *     <li> {@link org.eclipse.jetty.http.MimeTypes.Type#FORM_ENCODED_8859_1} </li>
     *     <li> {@link org.eclipse.jetty.http.MimeTypes.Type#FORM_ENCODED_UTF_8} </li>
     * </ul>
     * an empty form value will be returned, meaning {@code FormValues.isEmpty() == true}
     */
    public FormValues formValue() {
        return formValues;
    }

    public InputStream asInputStream() {
        return new ByteArrayInputStream(payload);
    }

    public <T> T json(Class<T> targetType) {
        return Bindings.json().bind(payload, targetType);
    }

    public <T> T xml(Class<T> targetType) {
        return Bindings.xml().bind(payload, targetType);
    }

    public String text() {
        return new String(payload, StandardCharsets.UTF_8);
    }

}
