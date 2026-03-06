package com.github.sinakarimi81.espresso.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Container<T> {

    private T containee;

}
