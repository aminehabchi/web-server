package com.http.utils;

import java.nio.charset.StandardCharsets;

public final class Default {

    // ===== Config file =====
    public static final String CONFIG_PATH = "application.properties";

    // ===== Server =====
    public static final int DEFAULT_PORT = 8080;
    public static final long DEFAULT_MAX_BODY_SIZE = 10 * 1024 * 1024; // 10MB
    public static final long DEFAULT_CONNECTION_TIMEOUT = 15_000; // 15s
    public static final long DEFAULT_REQUEST_TIMEOUT = 5_000;
    public static final int DEFAULT_THREAD_POOL = 10;
    public static final int DEFAULT_MAX_CONNECTIONS = 1_000;
    public static final int DEFAULT_BACKLOG = 100;
    public static final boolean DEFAULT_DEBUG = false;

    // ===== Session =====
    public static final String DEFAULT_SESSION_NAME = "SESSION_ID";
    public static final long DEFAULT_SESSION_TIMEOUT = 60 * 30; // 30 minutes
    public static final boolean DEFAULT_SESSION_SECURE = false;
    public static final boolean DEFAULT_SESSION_HTTP_ONLY = true;
    public static final String DEFAULT_SESSION_PATH = "/";
    public static final String DEFAULT_SESSION_SAME_SITE = "Lax";

    // ===== Connector =====
    public static final int MAX_HEADER_SIZE = 16 * 1024;
    public static final int MAX_QUEUE_SIZE = 1024;
    public static final long MAX_BODY_SIZE = Long.MAX_VALUE;
    public static final int MAX_BODY_RAM = 10 * 1024 * 1024;
    public static final int MAX_CHUNK_LINE = 2048;
    public static final byte[] HEADER_DELIMITER = new byte[] { '\r', '\n', '\r', '\n' };

    // ===== Response =====
    public static final byte[] TOO_LARGE_RESPONSE = ("HTTP/1.1 413 Payload Too Large\r\n" +
            "Content-Length: 17\r\n" +
            "Connection: close\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "Request too large").getBytes(StandardCharsets.UTF_8);

    public static final byte[] BAD_REQUEST_RESPONSE = ("HTTP/1.1 400 Bad Request\r\n" +
            "Content-Length: 11\r\n" +
            "Connection: close\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "Bad Request").getBytes(StandardCharsets.UTF_8);

    public static final byte[] MALFORMED_REQUEST_RESPONSE = ("HTTP/1.1 400 Bad Request\r\n" +
            "Content-Length: 17\r\n" +
            "Connection: close\r\n" +
            "Content-Type: text/plain\r\n" +
            "\r\n" +
            "Malformed request").getBytes(StandardCharsets.UTF_8);

}