package com.app.filters;

import com.http.annotation.classes.Filter;
import com.http.application.filters.OncePerRequestFilter;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Filter(order = 1)
public class LoggingFilter implements OncePerRequestFilter {

    @Override
    public boolean doFilter(HttpRequest request, HttpResponse response) {
        System.out.println("[" + request.getMethod() + "] " + request.getUrl().getRawUrl());
        return true;
    }
}
