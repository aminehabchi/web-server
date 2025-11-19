package com.http.application.routing;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, Method> postRoutes;
    private final Map<String, Method> getRoutes;
    private final Map<String, Method> deleteRoutes;

    public Router() {
        postRoutes = new HashMap<>();
        getRoutes = new HashMap<>();
        deleteRoutes = new HashMap<>();
    }

    public void addPostRoute() {

    }

    public Method getPostMethod(String path) {
        return null;
    }

    public void addGetRoute() {

    }

    public Method getGetMethod(String path) {
        return null;
    }

    public void addDeleteRoute() {

    }

    public Method getDeleteMethod(String path) {
        return null;
    }

}
