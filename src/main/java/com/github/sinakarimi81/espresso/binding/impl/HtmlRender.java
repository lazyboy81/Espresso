package com.github.sinakarimi81.espresso.binding.impl;

import com.github.sinakarimi81.espresso.binding.Serialization;
import com.github.sinakarimi81.espresso.binding.config.ThymeleafConfig;
import com.github.sinakarimi81.espresso.binding.dto.TemplateData;
import org.eclipse.jetty.http.MimeTypes;
import org.thymeleaf.context.Context;

public class HtmlRender implements Serialization {

    @Override
    public String serialize(Object payload) {
        var data = (TemplateData) payload;
        return ThymeleafConfig.engine().process(data.name(), new Context(data.locale(), data.vars()));
    }

    @Override
    public String contentTypeValue() {
        return MimeTypes.Type.TEXT_HTML_UTF_8.asString();
    }

    @Override
    public boolean canHandle(String acceptType) {
        return MimeTypes.Type.TEXT_HTML_UTF_8.asString().equalsIgnoreCase(acceptType) ||
                MimeTypes.Type.TEXT_HTML_8859_1.asString().equalsIgnoreCase(acceptType) ||
                MimeTypes.Type.TEXT_HTML.asString().equalsIgnoreCase(acceptType);
    }

}
