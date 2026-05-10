package com.github.sinakarimi81.espresso.binding;

import java.io.InputStream;

public abstract class Binding implements Serialization {

    public abstract  <T> T bind(InputStream payload, Class<T> tClass);

}
