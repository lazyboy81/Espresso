package com.github.sinakarimi81.espresso.context;

import com.github.sinakarimi81.espresso.engine.Engine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Context {

    private Engine engine;
    private InputStream request;
    private OutputStream response;

}
