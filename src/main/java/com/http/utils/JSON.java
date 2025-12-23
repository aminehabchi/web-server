package com.http.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSON {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    public static Object toObjectFromJson(String body, Class<?> type) {
        try {
            return objectMapper.readValue(body, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON body", e);
        }
    }
}
