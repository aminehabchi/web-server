package com.http.application.dispatcher;

import java.lang.reflect.Method;
import java.util.List;

import com.http.application.routing.*;
import com.http.config.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

public class Dispatcher {

    private final Router router;
    private final argumentResolvers argumentResolvers;

    public Dispatcher(Router router) {
        this.router = router;
        this.argumentResolvers = new argumentResolvers();
    }

    public HttpResponse dispatch(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        ;
        try {
            Route route = this.router.getRoute(request.getMethod(), request.getUrl().getPath());
            if (route != null) {
                // Get the handler method
                Method handler = route.getMethod();
                // Resolve method arguments
                Object[] args = this.argumentResolvers.resolveArguments(handler, request);
                // Invoke the handler method and get the result
                Object result = route.invoke(args);

                // 
                if (result instanceof HttpResponse) {
                    response = (HttpResponse) result;
                } else {
                    response.setBody(result.toString());
                }

            } else {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setBody("404 Not Found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    public List<?> getParameter(Method method) {

        return null;
    }

}
