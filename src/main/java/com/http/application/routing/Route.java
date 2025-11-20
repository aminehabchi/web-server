package com.http.application.routing;

import java.lang.reflect.Method;

class Route {
    private final String path;
    private final Method method;
    private final Object controllerInstance;

    public Route(String path, Method method, Object controllerInstance) {
        this.path = path;
        this.method = method;
        this.controllerInstance = controllerInstance;
    }

    public String getPath() {
        return path;
    }

    public Method getMethod() {
        return method;
    }

    public Object getControllerInstance() {
        return controllerInstance;
    }

    // Helper: invoke method easily
    public Object invoke(Object... args) throws Exception {
        return method.invoke(controllerInstance, args);
    }
}