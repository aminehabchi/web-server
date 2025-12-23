package com.http.testing;

import com.http.annotation.classes.Component;
import com.http.annotation.classes.Filter;
import com.http.application.filters.OncePerRequestFilter;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Component
@Filter(order = 2, exculde = { 8080 })
public class CutsomFilter2 implements OncePerRequestFilter {

    @Override
    public boolean doFilter(HttpRequest req, HttpResponse res) {
        System.out.println("hello from filter 2");
        return true;
    }

}
