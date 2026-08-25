package io.github.lazyboy81.espresso.jetty;

import lombok.Builder;

@Builder
public class JettyOptions {

    private final Integer acceptors;
    private final Integer selectors;
    private final Integer port;

    public static JettyOptions defaultOps() {
        return JettyOptions.builder().build();
    }

    public Integer getAcceptorsOrDefault() {
        if (acceptors == null) {
            return -1;
        }

        return acceptors;
    }

    public Integer getSelectorsOrDefault() {
        if (selectors == null) {
            return -1;
        }

        return selectors;
    }

    public Integer getPortOrDefault() {
        if (port == null) {
            return 8080;
        }

        return port;
    }
}
