package com.github.sinakarimi81.espresso.middleware;

public abstract class Middlewares {

    /**
     * The default format logs requests and response as JSON with the following fields:
     * for request:
     * <ul>
     *     <li>time: RFC3339 nano timestamp</li>
     *     <li>id: Request ID from X-Request-ID header</li>
     *     <li>remote_ip: Client IP address</li>
     *     <li>host: Host header</li>
     *     <li>method: HTTP method</li>
     *     <li>uri: Request URI</li>
     *     <li>user_agent: User-Agent header</li>
     *     <li>payload: the request message (if any)</li>
     * </ul>
     * example output:
     * <pre>{@code
     *  {
     *    "time": "2023-01-15T10:30:45.123456789Z",
     *    "id": "",
     *    "remote_ip": "127.0.0.1",
     *    "host": "localhost:8080",
     *    "method": "GET",
     *    "uri": "/users/123",
     *    "user_agent": "Mozilla/5.0",
     *    "payload": ""
     *  }
     *  }</pre>
     * <p>
     * for response:
     * <ul>
     *     <li>time: RFC3339 nano timestamp</li>
     *     <li>id: Request ID from X-Request-ID header</li>
     *     <li>latency: Processing time in nanoseconds</li>
     *     <li>latency_human: Human-readable processing time</li>
     *     <li>status: HTTP status code</li>
     *     <li>payload: the response message (if any)</li>
     * </ul>
     * example output:
     * <pre>{@code
     *  {
     *    "time": "2023-01-15T10:30:45.123456789Z",
     *    "id": "",
     *    "latency": 50000000,
     *    "latency_human": "50ms",
     *    "status": 200
     *    "payload": ""
     *  }
     *  }</pre>
     *
     * @return {@link RequestResponseLogger} middleware
     */
    public static Middleware requestResponseLogger() {
        return new RequestResponseLogger();
    }

    /**
     * <p>add a request id to the response headers as {@code X-Request-Id} header. if the request already has {@code X-Request-Id} header, then the value of that is used</p>
     * @return {@link RequestIdGenerator} middleware
     */
    public static Middleware requestId() {
        return new RequestIdGenerator();
    }

}
