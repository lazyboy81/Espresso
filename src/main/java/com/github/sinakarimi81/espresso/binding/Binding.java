package com.github.sinakarimi81.espresso.binding;

public abstract class Binding extends Serialization {

    public abstract  <T> T bind(String payload, Class<T> tClass);

}
