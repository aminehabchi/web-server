package com.http.application.dispatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.http.annotation.parameters.RequestBody;
import com.http.annotation.parameters.RequestHeader;
import com.http.annotation.parameters.RequestParam;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;
import com.http.utils.JSON;

public class ArgumentResolvers {
    public ArgumentResolvers() {
    }

    public Object[] resolveArguments(Method handler, HttpRequest request, HttpResponse response) {
        Parameter[] parameters = handler.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            try {
                // HttpRequest
                if (param.getType() == HttpRequest.class) {
                    args[i] = request;
                    continue;
                }

                // HttpResponse
                if (param.getType() == HttpResponse.class) {
                    args[i] = response;
                    continue;
                }

                // @RequestBody → deserialize JSON to object
                if (param.isAnnotationPresent(RequestBody.class)) {
                    Class<?> type = param.getType();
                    String body = request.getBody();

                    if (body == null || body.isEmpty()) {
                        throw new IllegalArgumentException("@RequestBody is missing or empty");
                    }

                    try {
                        Object obj = JSON.toObjectFromJson(body, type);
                        args[i] = obj;
                    } catch (RuntimeException e) {
                        throw new IllegalArgumentException("Failed to parse @RequestBody: " + e.getCause().getMessage(),e);
                    }
                    continue;
                }

                // @RequestHeader("headerName")
                if (param.isAnnotationPresent(RequestHeader.class)) {
                    RequestHeader headerAnn = param.getAnnotation(RequestHeader.class);
                    String value = request.getHeaders().get(headerAnn.value());
                    if (value == null)
                        throw new IllegalArgumentException("Missing required header: " + headerAnn.value());
                    args[i] = convertType(value, param.getType());
                    continue;
                }

                // @RequestParam("paramName")
                if (param.isAnnotationPresent(RequestParam.class)) {
                    RequestParam paramAnn = param.getAnnotation(RequestParam.class);
                    String value = request.getUrl().getQueryParams().get(paramAnn.value());
                    if (value == null)
                        throw new IllegalArgumentException("Missing required query parameter: " + paramAnn.value());
                    args[i] = convertType(value, param.getType());
                    continue;
                }

                // default null if not recognized
                args[i] = null;

            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Failed to resolve argument for parameter '" + param.getName() + "': " + e.getMessage(), e);
            }
        }

        return args;
    }

    private Object convertType(String value, Class<?> type) {
        if (value == null)
            return null;
        if (type == String.class)
            return value;
        if (type == int.class || type == Integer.class)
            return Integer.parseInt(value);
        if (type == long.class || type == Long.class)
            return Long.parseLong(value);
        if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(value);
        return value;
    }
}
