package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;

public interface Router {

    void options(String path, Handler handler);
    void head(String path, Handler handler);
    void get(String path, Handler handler);
    void post(String path, Handler handler);
    void put(String path, Handler handler);
    void delete(String path, Handler handler);
    void any(String path, Handler handler);
    Router group(String root);

}
