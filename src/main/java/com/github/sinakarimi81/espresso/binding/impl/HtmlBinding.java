package com.github.sinakarimi81.espresso.binding.impl;

import com.github.sinakarimi81.espresso.binding.Render;
import com.github.sinakarimi81.espresso.binding.config.ThymeleafConfig;
import com.github.sinakarimi81.espresso.binding.dto.HtmlData;
import org.thymeleaf.context.Context;

public class HtmlBinding extends Render {

    private static final String CONTENT_TYPE = "text/html; charset=utf-8";


    @Override
    public String convertResponsePayload(Object payload) {
        var data = (HtmlData) payload;
        return ThymeleafConfig.engine().process(data.templateName(), new Context(data.locale(), data.vars()));
    }

    @Override
    public String contentTypeValue() {
        return CONTENT_TYPE;
    }
}
