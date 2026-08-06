package io.github.lazyboy81.espresso.core.http.constants;

public enum MediaType {

    ALL("*", "*"),

    APPLICATION_ATOM_XML("application", "atom+xml"),

    APPLICATION_CBOR("application", "cbor"),

    APPLICATION_FORM_URLENCODED("application", "x-www-form-urlencoded"),

    APPLICATION_GRAPHQL_RESPONSE("application", "graphql-response+json"),

    APPLICATION_JSON("application", "json"),

    APPLICATION_OCTET_STREAM("application", "octet-stream"),

    APPLICATION_PDF("application", "pdf"),

    APPLICATION_PROBLEM_JSON("application", "problem+json"),

    APPLICATION_PROBLEM_XML("application", "problem+xml"),

    APPLICATION_PROTOBUF("application", "x-protobuf"),

    APPLICATION_RSS_XML("application", "rss+xml"),

    APPLICATION_NDJSON("application", "x-ndjson"),

    APPLICATION_XHTML_XML("application", "xhtml+xml"),

    APPLICATION_XML("application", "xml"),

    APPLICATION_YAML("application", "yaml"),

    IMAGE_GIF("image", "gif"),

    IMAGE_JPEG("image", "jpeg"),

    IMAGE_PNG("image", "png"),

    MULTIPART_FORM_DATA("multipart", "form-data"),

    MULTIPART_MIXED("multipart", "mixed"),

    MULTIPART_RELATED("multipart", "related"),

    TEXT_EVENT_STREAM("text", "event-stream"),

    TEXT_HTML("text", "html"),

    TEXT_MARKDOWN("text", "markdown"),

    TEXT_PLAIN("text", "plain"),

    TEXT_XML("text", "xml");

    private String type;
    private String subType;

    MediaType(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    public String value() {
        return type + "/" + subType;
    }
}
