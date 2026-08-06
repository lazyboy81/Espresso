package io.github.lazyboy81.espresso.core.binding;

public abstract class Binding implements Serialization {

    public abstract  <T> T bind(byte[] payload, Class<T> tClass);

}
