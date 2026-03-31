package com.http.application.configuration;

import java.util.List;
import java.util.Map;

import com.http.utils.Default;

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

    public List<Integer> getPorts() { return ports; }
    public void setPorts(List<Integer> ports) { this.ports = ports; }

    public long getMaxBodySize() { return maxBodySize; }
    public void setMaxBodySize(long maxBodySize) { this.maxBodySize = maxBodySize; }

    public long getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public int getThreadPool() { return threadPool; }
    public void setThreadPool(int threadPool) { this.threadPool = threadPool; }

    public int getMaxConnections() { return maxConnections; }
    public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

    public int getBacklog() { return backlog; }
    public void setBacklog(int backlog) { this.backlog = backlog; }

    public SessionConfig getSessionConfig() { return sessionConfig; }
    public void setSessionConfig(SessionConfig sessionConfig) { this.sessionConfig = sessionConfig; }

    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    public Map<String, String> getConfigs() { return configs; }
    public void setConfigs(Map<String, String> configs) { this.configs = configs; }
}
