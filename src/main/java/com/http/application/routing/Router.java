package com.http.application.routing;

import com.http.annotation.methodes.Delete;
import com.http.annotation.methodes.Get;
import com.http.annotation.methodes.Post;
import com.http.http.enums.HttpMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<String, Route> getRouteMap = new HashMap<>();
    private final Map<String, Route> postRouteMap = new HashMap<>();
    private final Map<String, Route> deleteRouteMap = new HashMap<>();

    private final Map<Class<?>, Object> controllerInstances;

    public Router(Map<Class<?>, Object> controllerInstances) {
        this.controllerInstances = controllerInstances;
        registerAllControllerRoutes(); // automatically register routes
    }

    private void registerAllControllerRoutes() {
        for (Object controller : controllerInstances.values()) {
            Class<?> clazz = controller.getClass();
            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get getAnnotation = method.getAnnotation(Get.class);
                    registerRoute(HttpMethod.GET, getAnnotation.path(), method, controller);
                } else if (method.isAnnotationPresent(Post.class)) {
                    Post postAnnotation = method.getAnnotation(Post.class);
                    registerRoute(HttpMethod.POST, postAnnotation.path(), method, controller);
                } else if (method.isAnnotationPresent(Delete.class)) {
                    Delete deleteAnnotation = method.getAnnotation(Delete.class);
                    registerRoute(HttpMethod.DELETE, deleteAnnotation.path(), method, controller);
                }
            }
        }
    }

    // Register a route with HttpMethod enum
    public void registerRoute(HttpMethod httpMethod, String path, Method method, Object controllerInstance) {
        Route route = new Route(path, method, controllerInstance);

        switch (httpMethod) {
            case GET -> getRouteMap.put(path, route);
            case POST -> postRouteMap.put(path, route);
            case DELETE -> deleteRouteMap.put(path, route);
        }

        System.out.println("Registered " + httpMethod + " route: " + path + " -> " + method.getName());
    }

    // Get route by HttpMethod enum
    public Route getRoute(HttpMethod httpMethod, String path) {
        return switch (httpMethod) {
            case GET -> getRouteMap.get(path);
            case POST -> postRouteMap.get(path);
            case DELETE -> deleteRouteMap.get(path);
            default -> null;
        };
    }

    // Call route dynamically
    public Object handle(HttpMethod httpMethod, String path, Object... args) throws Exception {
        Route route = getRoute(httpMethod, path);
        if (route == null) {
            throw new RuntimeException("404 Not Found: " + path);
        }
        return route.invoke(args);
    }

    // Print all controller classes
    public void printAllControllers() {
        for (Class<?> clazz : controllerInstances.keySet()) {
            System.out.println("Registered Controller: " + clazz.getName());
        }
    }
}
