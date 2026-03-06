package com.github.sinakarimi81.espresso.engine;

import com.github.sinakarimi81.espresso.exception.PathNotFoundException;
import com.github.sinakarimi81.espresso.handler.Handler;
import com.github.sinakarimi81.espresso.parsing.Parser;
import com.github.sinakarimi81.espresso.routing.MethodTree;
import com.github.sinakarimi81.espresso.routing.MethodTrees;
import com.github.sinakarimi81.espresso.util.Tuple;

public class Engine {

    private final MethodTrees trees;

    public Engine(MethodTrees methodTrees) {
        trees = methodTrees;
    }

    public Handler getHandlerForEndpoint(String url) {
        Parser.validateHttpVersion(url);

        Tuple<String, String> methodAndPath = Parser.getMethodAndPath(url);
        MethodTree methodTree = trees.get(methodAndPath.left());

        if (methodTree == null) {
            throw new PathNotFoundException(String.format("no mapping for given url was found: %s", url), url);
        }

        return methodTree.getHandlerForPath(methodAndPath.right());
    }

}
