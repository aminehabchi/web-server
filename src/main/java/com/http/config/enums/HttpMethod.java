package com.http.config.enums;

public enum HttpMethod {
    GET,
    POST,
    DELETE;

    public static HttpMethod fromReason(String reason) {
        if (reason == null || reason.isEmpty()) {
            return null;
        }
        
        try {
            return HttpMethod.valueOf(reason.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
