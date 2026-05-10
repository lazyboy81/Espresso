package com.github.sinakarimi81.espresso.binding;

public interface Serialization {

    String serialize(Object payload);
    String contentTypeValue();
    boolean canHandle(String acceptType);

}
