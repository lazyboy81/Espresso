package com.github.sinakarimi81.espresso.binding;

import com.github.sinakarimi81.espresso.binding.impl.HtmlRender;
import com.github.sinakarimi81.espresso.binding.impl.JsonBinding;
import com.github.sinakarimi81.espresso.binding.impl.TextBinding;
import com.github.sinakarimi81.espresso.binding.impl.XmlBinding;
import org.eclipse.jetty.server.Server;

import java.util.List;
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
