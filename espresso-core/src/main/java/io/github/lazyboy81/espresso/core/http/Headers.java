package io.github.lazyboy81.espresso.core.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Headers(Map<String, String> value) {

    public Headers(Map<String, String> value) {
        this.value = new HashMap<>(value);
    }

    public static Headers from(Map<String, String> value) {
        return new Headers(value);
    }

    public boolean containsHeader(String name) {
        return this.value.containsKey(name);
    }

    public List<String> getHeaderValues(String name) {
        return Arrays.stream(
                this.value.get(name).split(",")
        ).toList();
    }

    public String getHeaderValue(String name) {
        return this.value.get(name);
    }

    public boolean hasValue(String name, String value) {
        if (!this.value.containsKey(name)) {
            return false;
        }

        return this.value.get(name).contains(value);
    }

}
