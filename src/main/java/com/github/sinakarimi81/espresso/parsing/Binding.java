package com.github.sinakarimi81.espresso.parsing;

public interface Binding {

    <T> T bind(String payload, Class<T> tClass);

}
