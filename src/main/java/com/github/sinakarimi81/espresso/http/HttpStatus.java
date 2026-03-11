package com.github.sinakarimi81.espresso.http;

public enum HttpStatus {

    // 1xx statuses

    /**
     * {@code 100 Continue}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-100-continue">HTTP Semantics, section 15.2.1</a>
     */
    CONTINUE(100, Series.INFORMATIONAL, "Continue"),

    // 2xx statuses

    /**
     * {@code 200 OK}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-200-ok">HTTP Semantics, section 15.3.1</a>
     */
    OK(200, Series.SUCCESSFUL, "OK"),
    /**
     * {@code 201 Created}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-201-created">HTTP Semantics, section 15.3.2</a>
     */
    CREATED(201, Series.SUCCESSFUL, "Created"),
    /**
     * {@code 204 No Content}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-204-no-content">HTTP Semantics, section 15.3.5</a>
     */
    NO_CONTENT(204, Series.SUCCESSFUL, "No Content"),

    // 3xx statuses

    /**
     * {@code 301 Moved Permanently}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-301-moved-permanently">HTTP Semantics, section 15.4.2</a>
     */
    MOVED_PERMANENTLY(301, Series.REDIRECTION, "Moved Permanently"),
    /**
     * {@code 302 Found}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-302-found">HTTP Semantics, section 15.4.3</a>
     */
    FOUND(302, Series.REDIRECTION, "Found"),
    /**
     * {@code 304 Not Modified}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-304-not-modified">HTTP Semantics, section 15.4.5</a>
     */
    NOT_MODIFIED(304, Series.REDIRECTION, "Not Modified"),

    // 4xx statuses

    /**
     * {@code 400 Bad Request}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-400-bad-request">HTTP Semantics, section 15.5.1</a>
     */
    BAD_REQUEST(400, Series.CLIENT_ERROR, "Bad Request"),
    /**
     * {@code 404 Not Found}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-404-not-found">HTTP Semantics, section 15.5.5</a>
     */
    NOT_FOUND(404, Series.CLIENT_ERROR, "Not Found"),
    /**
     * {@code 405 Method Not Allowed}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-405-method-not-allowed">HTTP Semantics, section 15.5.6</a>
     */
    METHOD_NOT_ALLOWED(405, Series.CLIENT_ERROR, "Method Not Allowed"),

    // 5xx statuses

    /**
     * {@code 500 Internal Server Error}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#section-15.6.1">HTTP Semantics, section 15.6.1</a>
     */
    INTERNAL_SERVER_ERROR(500, Series.SERVER_ERROR, "Internal Server Error"),
    /**
     * {@code 502 Bad Gateway}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-502-bad-gateway">HTTP Semantics, section 15.6.3</a>
     */
    BAD_GATEWAY(502, Series.SERVER_ERROR, "Bad Gateway"),
    /**
     * {@code 503 Service Unavailable}.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc9110#name-503-service-unavailable">HTTP Semantics, section 15.6.4</a>
     */
    SERVICE_UNAVAILABLE(503, Series.SERVER_ERROR, "Service Unavailable");

    private final int code;
    private final Series series;
    private final String description;

    HttpStatus(int code, Series series, String description) {
        this.code = code;
        this.series = series;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    private Series series() {
        return series;
    }

    public boolean is1xxInformational() {
        return (series() == Series.INFORMATIONAL);
    }

    public boolean is2xxSuccessful() {
        return (series() == Series.SUCCESSFUL);
    }

    public boolean is3xxRedirection() {
        return (series() == Series.REDIRECTION);
    }

    public boolean is4xxClientError() {
        return (series() == Series.CLIENT_ERROR);
    }

    public boolean is5xxServerError() {
        return (series() == Series.SERVER_ERROR);
    }

    public boolean isError() {
        return (is4xxClientError() || is5xxServerError());
    }

    private enum Series {
        INFORMATIONAL,
        SUCCESSFUL,
        REDIRECTION,
        CLIENT_ERROR,
        SERVER_ERROR
    }
}
