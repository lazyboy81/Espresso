package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.util.StringUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupedRoutes implements Router {

    private final Routes routes;
    private final String rootPath;

    @Override
    public void options(String path, Handler handler) {
        StringUtils.validUrlInput(path);
        routes.options(rootPath + path, handler);
    }

    @Override
    public void head(String path, Handler handler) {
        StringUtils.validUrlInput(path);
        routes.head(rootPath + path, handler);
    }

    @Override
    public void get(String path, Handler handler) {
        StringUtils.validUrlInput(path);
        routes.get(rootPath + path, handler);
    }

    @Override
    public void post(String path, Handler handler) {
        StringUtils.validUrlInput(path);
        routes.post(rootPath + path, handler);
    }

    @Override
    public void put(String path, Handler handler) {
        StringUtils.validUrlInput(path);
        routes.put(rootPath + path, handler);
    }

    @Override
    public void delete(String path, Handler handler) {
        StringUtils.validUrlInput(path);
        routes.delete(rootPath + path, handler);
    }

    @Override
    public void any(String path, Handler handler) {
        StringUtils.validUrlInput(path);
        routes.any(rootPath + path, handler);
    }


    @Override
    public Router group(String root) {
        StringUtils.validUrlInput(root);
        return routes.group(rootPath + root);
    }
}
