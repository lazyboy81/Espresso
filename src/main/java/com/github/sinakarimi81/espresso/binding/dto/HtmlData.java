package com.github.sinakarimi81.espresso.binding.dto;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public record HtmlData(String templateName, Locale locale, Map<String, Object> vars) {
    public HtmlData {
        if (Objects.isNull(templateName) || templateName.isBlank()) {
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
