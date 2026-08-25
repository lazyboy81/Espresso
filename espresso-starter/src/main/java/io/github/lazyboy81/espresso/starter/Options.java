package io.github.lazyboy81.espresso.starter;

import io.github.lazyboy81.espresso.jetty.JettyOptions;

public class Options {

    public static JettyOptions.JettyOptionsBuilder jetty() {
        return JettyOptions.builder();
    }

}
