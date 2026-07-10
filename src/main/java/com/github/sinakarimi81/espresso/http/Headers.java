package com.github.sinakarimi81.espresso.http;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;

import java.util.List;

/**
 * <p>An interface for value received by server from a request</p>
 */
public record Headers(HttpFields value) {

    public static Headers from(HttpFields headers) {
        return new Headers(headers);
    }

    public boolean containsHeader(HttpHeader name) {
        return this.value.contains(name);
    }

    public List<String> getHeaderValues(HttpHeader name) {
        return this.value.getValuesList(name);
    }

    public String getHeaderValue(HttpHeader name) {
        return this.value.get(name);
    }

    public boolean hasValue(HttpHeader name, String value) {
        return this.value.contains(name, value);
    }

    public boolean containsHeader(String name) {
        return this.value.contains(name);
    }

    public List<String> getHeaderValues(String name) {
        return this.value.getValuesList(name);
    }

    public String getHeaderValue(String name) {
        return this.value.get(name);
    }

    public boolean hasValue(String name, String value) {
        return this.value.contains(name, value);
    }

}
