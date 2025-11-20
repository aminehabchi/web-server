package com.http.application;

import com.http.application.annotation.classes.Controller;
import com.http.application.annotation.classes.Component;
import com.http.application.annotation.classes.Service;
import com.http.application.annotation.classes.Repository;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

public class ApplicationContext {

    // Map Class -> instance (IoC container)
    private final Map<Class<?>, Object> beans = new HashMap<>();

    public ApplicationContext() {
        try {
            Set<Class<?>> classes = scanForAnnotatedClasses();
            createBeans(classes); // create instances
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Scan all classes with @Controller, @Component, @Service, @Repository
    private Set<Class<?>> scanForAnnotatedClasses() {
        Reflections reflections = new Reflections("com.http");

        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(reflections.getTypesAnnotatedWith(Controller.class));
        classes.addAll(reflections.getTypesAnnotatedWith(Service.class));
        classes.addAll(reflections.getTypesAnnotatedWith(Component.class));
        classes.addAll(reflections.getTypesAnnotatedWith(Repository.class));

        return classes;
    }

    // Create instances for all classes and store in beans map
    private void createBeans(Set<Class<?>> classes) throws Exception {
        for (Class<?> clazz : classes) {
            if (!beans.containsKey(clazz)) {
                Object instance = createInstance(clazz);
                beans.put(clazz, instance);
            }
        }
    }

    // Handle dependencies via constructor injection
    private Object createInstance(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getConstructors()[0]; // first constructor
        Class<?>[] paramTypes = constructor.getParameterTypes();

        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Object dependency = beans.get(paramTypes[i]);
            if (dependency == null) {
                dependency = createInstance(paramTypes[i]);
                beans.put(paramTypes[i], dependency);
            }
            params[i] = dependency;
        }

        return constructor.newInstance(params);
    }

    public Map<Class<?>, Object> getControllerBeans() {
        Map<Class<?>, Object> controllers = new HashMap<>();
        for (Map.Entry<Class<?>, Object> entry : beans.entrySet()) {
            if (entry.getKey().isAnnotationPresent(Controller.class)) {
                controllers.put(entry.getKey(), entry.getValue());
            }
        }
        return controllers;
    }

}
