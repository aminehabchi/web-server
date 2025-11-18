package com.http.config.connectors;

import com.http.config.defaults.DefaultConnectorConfig;

public class Connector {
    private int port;
    private long requestTimeout;

    Connector() {
        this.port = DefaultConnectorConfig.DEFAULT_PORT;
        this.requestTimeout = DefaultConnectorConfig.DEFAULT_REQUEST_TIMEOUT;
    }

    Connector(int port) {
        this.validatePort(port);
        this.port = port;
        this.requestTimeout = DefaultConnectorConfig.DEFAULT_REQUEST_TIMEOUT;
    }

    Connector(long requestTimeout) {
        this.requestTimeout = requestTimeout;
        this.validateRequestTimeout(requestTimeout);
        this.port = DefaultConnectorConfig.DEFAULT_PORT;
    }

    Connector(int port, long requestTimeout) {
        this.validateRequestTimeout(requestTimeout);
        this.validatePort(port);
        this.port = port;
        this.requestTimeout = requestTimeout;
    }

    void connect() {
        // Implementation for establishing a connection
    }

    void disconnect() {
        // Implementation for disconnecting
    }


    int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }

    public void validatePort(int port) throws IllegalArgumentException {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port number must be between 1 and 65535");
        }
    }  
 
    public void validateRequestTimeout(long requestTimeout) throws IllegalArgumentException {
        if (requestTimeout < 0) {
            throw new IllegalArgumentException("Request timeout must be non-negative");
        }
    }

    long getRequestTimeout() {
        return requestTimeout;
    }

    void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
}
