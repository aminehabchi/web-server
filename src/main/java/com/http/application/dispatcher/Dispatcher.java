package com.http.application.dispatcher;

import java.lang.reflect.Method;

import com.http.application.routing.*;
import com.http.config.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;
import com.http.utils.JSON;

public class Dispatcher extends ArgumentResolvers {
    private final Router router;

    public Dispatcher(Router router) {
        super();
        this.router = router;
    }

    public void dispatch(HttpRequest request, HttpResponse response) {
        try {
            System.out.println(request.getUrl());
            // 1. Find matching route
            Route route = this.router.getRoute(request.getMethod(), request.getUrl().getPath());
            if (route == null) {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setBody("404 Not Found");
                response.addHeader("Content-Type", "text/plain");
                response.addHeader("Content-Length", "13");
                return;
            }

            // 2. Resolve method + arguments
            Method handlerMethod = route.getMethod();
            Object[] args = null;
            try {
                args = resolveArguments(handlerMethod, request, response);
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setBody("400 Bad Request: " + e.getMessage());
                response.addHeader("Content-Type", "text/plain");
                response.addHeader("Content-Length", String.valueOf(response.getBody().length()));
                return;
            }

            // 3. Invoke controller handler
            Object result = route.invoke(args);

            // 4. Interpret result
            handleReturnValue(result, response);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void handleReturnValue(Object result, HttpResponse response) {

        // 1. If handler returns HttpResponse → copy it
        if (result instanceof HttpResponse httpResponse) {
            response = httpResponse;
            return;
        }

        // 2. If null or void → empty response
        if (result == null) {
            response.setBody("");
            response.addHeader("Content-Type", "text/plain");
            response.addHeader("Content-Length", "0");
            return;
        }

        // 3. If String → send as HTML
        if (result instanceof String str) {
            response.setBody(str);
            response.addHeader("Content-Type", "text/html");
            response.addHeader("Content-Length", String.valueOf(str.length()));
            return;
        }

        // 4. If Number → send as text/plain
        if (result instanceof Number num) {
            String body = num.toString();
            response.setBody(body);
            response.addHeader("Content-Type", "text/plain");
            response.addHeader("Content-Length", String.valueOf(body.length()));
            return;
        }

        // 5. Otherwise → convert object to JSON
        String json = JSON.toJson(result);
        response.setBody(json);
        response.addHeader("Content-Type", "application/json");
        response.addHeader("Content-Length", String.valueOf(json.length()));
    }

}
