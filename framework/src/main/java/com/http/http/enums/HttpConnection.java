package com.http.http.enums;

public enum HttpConnection {

    KEEP_ALIVE("keep-alive"),
    CLOSE("close");

    private final String value;

    HttpConnection(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static HttpConnection fromHeader(String header) {
        if (header == null) {
            return KEEP_ALIVE; 
        }

        if (header.equalsIgnoreCase("close")) {
            return CLOSE;
        }

        return KEEP_ALIVE;
    }
}
