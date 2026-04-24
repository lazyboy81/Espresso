package com.github.sinakarimi81.espresso.binding;

import com.github.sinakarimi81.espresso.binding.impl.HtmlRender;
import com.github.sinakarimi81.espresso.binding.impl.JsonBinding;
import com.github.sinakarimi81.espresso.binding.impl.TextBinding;
import com.github.sinakarimi81.espresso.binding.impl.XmlBinding;

public class Bindings {

    private static final JsonBinding json = new JsonBinding();
    private static final XmlBinding xml = new XmlBinding();
    private static final TextBinding text = new TextBinding();
    private static final HtmlRender html = new HtmlRender();

    private Bindings() {
    }

    public static JsonBinding json() {
        return json;
    }

    public static XmlBinding xml() {
        return xml;
    }

    public static TextBinding text() {
        return text;
    }

    public static HtmlRender html() {
        return html;
    }

}
