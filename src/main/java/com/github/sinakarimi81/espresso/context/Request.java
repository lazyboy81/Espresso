package com.github.sinakarimi81.espresso.context;

import com.github.sinakarimi81.espresso.http.Headers;

public record Request(Headers headers, String payload) {
}
