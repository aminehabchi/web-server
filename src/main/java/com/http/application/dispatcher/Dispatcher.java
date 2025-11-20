package com.http.application.dispatcher;

import com.http.application.routing.Router;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

public class Dispatcher {

    private final Router router;

    public Dispatcher(Router router) {
        this.router = router;
    }

    public HttpResponse dispatch(HttpRequest request) {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
