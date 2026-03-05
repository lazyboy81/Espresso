package com.github.sinakarimi81.espresso.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MethodTrees {

    private final Map<String, MethodTree> trees;

    public MethodTrees() {
        trees = new HashMap<>();
    }

    public MethodTree get(String name) {
        return trees.get(name);
    }

    public void add(String method) {
        trees.put(method, new MethodTree(method, new PathNode("/", "/", new ArrayList<>(), null)));
    }

}
