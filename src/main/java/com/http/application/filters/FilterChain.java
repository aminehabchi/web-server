package com.http.application.filters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.http.annotation.classes.Filter;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

public class FilterChain {
    private List<Object> filters = new ArrayList<>();

    public FilterChain(Map<Class<?>, Object> instances) {
        sortFilters(instances);
    }

    private void sortFilters(Map<Class<?>, Object> instances) {
        filters = instances.values().stream()
                .sorted(Comparator.comparingInt(o -> {
                    Filter filter = o.getClass().getAnnotation(Filter.class);
                    return filter != null ? filter.order() : Integer.MAX_VALUE;
                }))
                .collect(Collectors.toList());
    }

    public boolean applyFilters(HttpRequest request, HttpResponse response) {
        for (Object filter : filters) {
            if (checkIfExclude(filter, request.getPort())) {
                continue;
            }

            try {
                boolean result = (boolean) filter.getClass()
                        .getMethod("doFilter", HttpRequest.class, HttpResponse.class)
                        .invoke(filter, request, response);

                if (!result) { // stop chain if filter returns false
                    return false;
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke doFilter", e);
            }
        }
        return true;
    }

    public boolean checkIfExclude(Object filter, int port) {
        Filter annotation = filter.getClass().getAnnotation(Filter.class);
        if (annotation != null) {
            int[] excludedPorts = annotation.exculde();
            for (int p : excludedPorts) {
                if (p == port) {
                    return true;
                }
            }
        }
        return false;
    }

}
