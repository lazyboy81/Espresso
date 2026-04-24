package com.github.sinakarimi81.espresso.http;

import java.io.Serializable;

/**
 * A class that adds support for quality parameters as defined in the HTTP specification.
 *
 * <p>This class is meant to reference media types supported by Espresso. Spring Frameworks MediaType class was used
 * as a template for this class.
 *
 * @author Sina Karimi
 * @since 1.0
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.1">
 *     HTTP 1.1: Semantics and Content, section 3.1.1.1</a>
 */
public class MediaType implements Serializable {

    private final String type;
    private final String subType;

    private MediaType(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    @Override
    public String toString() {
        return type + "/" + subType;
    }

    public static final MediaType ALL;

    /**
     * A String equivalent of {@link MediaType#ALL}.
     */
    public static final String ALL_VALUE = "*/*";

    /**
     * Media type for {@code application/x-www-form-urlencoded}.
     */
    public static final MediaType APPLICATION_FORM_URLENCODED;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_FORM_URLENCODED}.
     */
    public static final String APPLICATION_FORM_URLENCODED_VALUE = "application/x-www-form-urlencoded";


    /**
     * Media type for {@code application/json}.
     */
    public static final MediaType APPLICATION_JSON;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_JSON}.
     */
    public static final String APPLICATION_JSON_VALUE = "application/json";

    /**
     * Media type for {@code application/octet-stream}.
     */
    public static final MediaType APPLICATION_OCTET_STREAM;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_OCTET_STREAM}.
     */
    public static final String APPLICATION_OCTET_STREAM_VALUE = "application/octet-stream";

    /**
     * Media type for {@code application/xml}.
     */
    public static final MediaType APPLICATION_XML;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_XML}.
     */
    public static final String APPLICATION_XML_VALUE = "application/xml";

    /**
     * Media type for {@code application/yaml}.
     */
    public static final MediaType APPLICATION_YAML;

    /**
     * A String equivalent of {@link MediaType#APPLICATION_YAML}.
     */
    public static final String APPLICATION_YAML_VALUE = "application/yaml";


    /**
     * Media type for {@code multipart/form-data}.
     */
    public static final MediaType MULTIPART_FORM_DATA;

    /**
     * A String equivalent of {@link MediaType#MULTIPART_FORM_DATA}.
     */
    public static final String MULTIPART_FORM_DATA_VALUE = "multipart/form-data";

    /**
     * Media type for {@code text/html}.
     */
    public static final MediaType TEXT_HTML;

    /**
     * A String equivalent of {@link MediaType#TEXT_HTML}.
     */
    public static final String TEXT_HTML_VALUE = "text/html";

    /**
     * Media type for {@code text/plain}.
     */
    public static final MediaType TEXT_PLAIN;

    /**
     * A String equivalent of {@link MediaType#TEXT_PLAIN}.
     */
    public static final String TEXT_PLAIN_VALUE = "text/plain";

    /**
     * Media type for {@code text/xml}.
     */
    public static final MediaType TEXT_XML;

    /**
     * A String equivalent of {@link MediaType#TEXT_XML}.
     */
    public static final String TEXT_XML_VALUE = "text/xml";


    static {
        // Not using "valueOf" to avoid static init cost
        ALL = new MediaType("*", "*");
        APPLICATION_FORM_URLENCODED = new MediaType("application", "x-www-form-urlencoded");
        APPLICATION_JSON = new MediaType("application", "json");
        APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");
        APPLICATION_XML = new MediaType("application", "xml");
        APPLICATION_YAML = new MediaType("application", "yaml");
        MULTIPART_FORM_DATA = new MediaType("multipart", "form-data");
        TEXT_HTML = new MediaType("text", "html");
        TEXT_PLAIN = new MediaType("text", "plain");
        TEXT_XML = new MediaType("text", "xml");
    }


}
