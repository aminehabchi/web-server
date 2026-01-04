package com.http.utils;

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
}