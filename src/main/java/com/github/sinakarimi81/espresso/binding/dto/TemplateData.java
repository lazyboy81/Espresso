package com.github.sinakarimi81.espresso.binding.dto;

import lombok.Builder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * the data needed to serialize and render an HTML template
 * @param name name of the HTML template to serialize
 * @param locale locale to use to serialize the HTML
 * @param vars value for any placeholders defined in the template
 */
@Builder
public record TemplateData(String name, Locale locale, Map<String, Object> vars) {
    public TemplateData {
        if (Objects.isNull(name) || name.isBlank()) {
            throw new IllegalArgumentException("template name cannot be null/blank");
        }

        if (locale == null) {
            locale = Locale.getDefault();
        }

        if (vars == null || vars.isEmpty()) {
            vars = new HashMap<>();
        }
    }
}
