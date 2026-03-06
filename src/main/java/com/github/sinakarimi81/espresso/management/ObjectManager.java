package com.github.sinakarimi81.espresso.management;

import com.github.sinakarimi81.espresso.engine.Engine;
import com.github.sinakarimi81.espresso.routing.MethodTrees;
import com.github.sinakarimi81.espresso.routing.RouteDefinition;

public class ObjectManager {
    
    private static ObjectManager INSTANCE;
    
    private Engine engine;
    private MethodTrees methodTrees;
    private RouteDefinition routeDefinition;
    
    private ObjectManager() {
        methodTrees = new MethodTrees();
        engine = new Engine(methodTrees);
        routeDefinition = new RouteDefinition(methodTrees);
    }
    
    public static ObjectManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ObjectManager();
        }
        return INSTANCE;
    }
    
    public Engine engine() {
        return engine;
    }

    public MethodTrees methodTrees() {
        return methodTrees;
    }

    public RouteDefinition routeDefinition() {
        return routeDefinition;
    }
}
