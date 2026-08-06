package io.github.lazyboy81.espresso.core.binding;

import io.github.lazyboy81.espresso.core.binding.impl.HtmlRender;
import io.github.lazyboy81.espresso.core.binding.impl.JsonBinding;
import io.github.lazyboy81.espresso.core.binding.impl.TextBinding;
import io.github.lazyboy81.espresso.core.binding.impl.XmlBinding;

import java.util.stream.Stream;

// by making the class abstract we wouldn't need to worry about in being instantiated
public abstract class Bindings {

    private static final JsonBinding json = new JsonBinding();
    private static final XmlBinding xml = new XmlBinding();
    private static final TextBinding text = new TextBinding();
    private static final HtmlRender html = new HtmlRender();

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

    public static Serialization find(String acceptType) {
        if (acceptType.isBlank()) {
            return json;
        }

        return Stream.of(json, xml, text, html)
                .filter(b -> b.canHandle(acceptType))
                .findAny()
                .orElse(json);
    }

}
