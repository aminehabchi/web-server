package com.http.application.dispatcher;

import java.lang.reflect.Method;

import com.http.http.request.HttpRequest;

// This class will handle argument resolution for controller methods
public class argumentResolvers {
    public argumentResolvers() {
    }

    public Object[] resolveArguments(Method handler, HttpRequest request) {
        return new Object[]{};
    }
}
