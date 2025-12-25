package com.http.config.configFile;

import java.util.List;
import java.util.Map;

public class ConfigServer {

    private List<String> hosts; // e.g., ["127.0.0.1", "0.0.0.0"]
    private List<Integer> ports; // e.g., [80, 8080]
    private boolean isDefault; // default server selection
    private Map<Integer, String> errorPages; // custom error pages {404: "/errors/404.html"}
    private long clientMaxBodySize; // in bytes, e.g., 1048576

    private List<ConfigRoute> routes;

    // Getters and Setters
    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void setPorts(List<Integer> ports) {
        this.ports = ports;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public Map<Integer, String> getErrorPages() {
        return errorPages;
    }

    public void setErrorPages(Map<Integer, String> errorPages) {
        this.errorPages = errorPages;
    }

    public long getClientMaxBodySize() {
        return clientMaxBodySize;
    }

    public void setClientMaxBodySize(long clientMaxBodySize) {
        this.clientMaxBodySize = clientMaxBodySize;
    }

    public List<ConfigRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(List<ConfigRoute> routes) {
        this.routes = routes;
    }

}