package io.github.lazyboy81.espresso.core.engine;

public interface ServerEngine extends AutoCloseable {
    void start();
    void stop();
    void join();
    int boundPort();
}
