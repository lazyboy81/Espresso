package com.github.sinakarimi81.espresso.context;

import com.github.sinakarimi81.espresso.http.Headers;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Request {

    @Getter
    private Headers headers;
    private String payload;



}
