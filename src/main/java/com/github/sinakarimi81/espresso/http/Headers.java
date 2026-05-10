package com.github.sinakarimi81.espresso.http;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;

import java.util.List;

/**
 * <p>An interface for headers received by server from a request</p>
 */
public record Headers(HttpFields headers) {

    public static Headers from(HttpFields headers) {
        return new Headers(headers);
    }

    public boolean containsHeader(HttpHeader name) {
        return headers.contains(name);
    }

    public List<String> getHeaderValues(HttpHeader name) {
        return headers.getValuesList(name);
    }

    public String getHeaderValue(HttpHeader name) {
        return headers.get(name);
    }

    public boolean hasValue(HttpHeader name, String value) {
        return headers.contains(name, value);
    }

    public boolean containsHeader(String name) {
        return headers.contains(name);
    }

    public List<String> getHeaderValues(String name) {
        return headers.getValuesList(name);
    }

    public String getHeaderValue(String name) {
        return headers.get(name);
    }

    public boolean hasValue(String name, String value) {
        return headers.contains(name, value);
    }

}
