package io.github.lazyboy81.espresso.core.binding.impl;

import io.github.lazyboy81.espresso.core.binding.Serialization;
import io.github.lazyboy81.espresso.core.binding.config.ThymeleafConfig;
import io.github.lazyboy81.espresso.core.binding.dto.TemplateData;
import io.github.lazyboy81.espresso.core.http.constants.MediaType;
import org.thymeleaf.context.Context;

public class HtmlRender implements Serialization {

    @Override
    public String serialize(Object payload) {
        var data = (TemplateData) payload;
        return ThymeleafConfig.engine().process(data.name(), new Context(data.locale(), data.vars()));
    }

    @Override
    public String contentTypeValue() {
        return MediaType.TEXT_HTML.value();
    }

    @Override
    public boolean canHandle(String acceptType) {
        return MediaType.TEXT_HTML.value().equalsIgnoreCase(acceptType);
    }

}
