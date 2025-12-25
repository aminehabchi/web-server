package com.http.config.enums;

import java.util.HashMap;
import java.util.Map;

public enum HttpStatus {
    // 2xx Success
    OK(200, "OK"),
    CREATED(201, "Created"),
    NO_CONTENT(204, "No Content"),

    // 3xx Redirection
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),

    // 4xx Client Errors
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    PAYLOAD_TOO_LARGE(413, "Payload Too Large"),

    // 5xx Server Errors
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway");

    private final int code;
    private final String reason;

    private static final Map<Integer, HttpStatus> CODE_MAP = new HashMap<>();
    private static final Map<String, HttpStatus> REASON_MAP = new HashMap<>();

    // static block to populate maps
    static {
        for (HttpStatus status : values()) {
            CODE_MAP.put(status.code, status);
            REASON_MAP.put(status.reason.toUpperCase(), status);
        }
    }

    HttpStatus(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return code + " " + reason;
    }

    public static HttpStatus fromCode(int code) {
        return CODE_MAP.getOrDefault(code, INTERNAL_SERVER_ERROR);
    }

    public static HttpStatus fromReason(String reason) {
        if (reason == null)
            return INTERNAL_SERVER_ERROR;
        return REASON_MAP.getOrDefault(reason.toUpperCase(), INTERNAL_SERVER_ERROR);
    }
}
