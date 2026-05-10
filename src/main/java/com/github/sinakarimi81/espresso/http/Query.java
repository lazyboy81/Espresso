package com.github.sinakarimi81.espresso.http;

import org.eclipse.jetty.util.Fields;

import java.util.List;
import java.util.Set;

public record Query(Fields params) {

    public static Query from(Fields params) {
        return new Query(params);
    }

    public Set<String> getAllNames() {
        return params.getNames();
    }

    public String get(String key) {
        return params.getValue(key);
    }

    public List<String> getValues(String key) {
        return params.getValuesOrEmpty(key);
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

    public boolean hasMultipleValues(String key) {
        return params.get(key).hasMultipleValues();
    }

}
