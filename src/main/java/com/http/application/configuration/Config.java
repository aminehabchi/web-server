package com.http.application.configuration;

import java.util.List;
import java.util.Map;

import com.http.utils.Default;

import lombok.Data;

@Data
public class Config {
    private List<Integer> ports = List.of(Default.DEFAULT_PORT);
    private long maxBodySize = Default.DEFAULT_MAX_BODY_SIZE;
    private long connectionTimeout = Default.DEFAULT_CONNECTION_TIMEOUT;
    private int threadPool = Default.DEFAULT_THREAD_POOL;
    private int maxConnections = Default.DEFAULT_MAX_CONNECTIONS;
    private int backlog = Default.DEFAULT_BACKLOG;
    private SessionConfig sessionConfig = new SessionConfig();
    private boolean debug = Default.DEFAULT_DEBUG;
    private Map<String, String> configs = Map.of();
}
