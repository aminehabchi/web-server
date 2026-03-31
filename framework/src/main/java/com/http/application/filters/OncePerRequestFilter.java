package com.http.application.filters;

import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

public interface OncePerRequestFilter {
    /**
     * Apply the filter.
     * 
     * @param req the request
     * @param res the response
     * @return true if the chain should continue, false to stop
     */
    boolean doFilter(HttpRequest req, HttpResponse res);
}
