package com.http.application.filters;

import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

public interface Filter {
    boolean doFilter(HttpRequest req, HttpResponse res);
}
