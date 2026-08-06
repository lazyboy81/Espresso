package io.github.lazyboy81.espresso.core.binding;

public interface Serialization {

    String serialize(Object payload);
    String contentTypeValue();
    boolean canHandle(String acceptType);

}
