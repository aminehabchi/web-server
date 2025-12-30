package com.http.config.configFile;

import java.util.Map;
import java.util.Set;

public class ConfigServer {

    private Set<String> hosts; // e.g., ["127.0.0.1", "0.0.0.0"]
    private Set<Integer> ports; // e.g., [80, 8080]
    private boolean isDefault; // default server selection
    private Map<Integer, String> errorPages; // custom error pages {404: "/errors/404.html"}
    private long clientMaxBodySize; // in bytes, e.g., 1048576

    private Map<String, ConfigRoute> routes;

    // Getters and Setters
    public Set<String> getHosts() {
        return hosts;
    }

    public void setHosts(Set<String> hosts) {
        this.hosts = hosts;
    }

    public Set<Integer> getPorts() {
        return ports;
    }

    public void setPorts(Set<Integer> ports) {
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

    public Map<String, ConfigRoute> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, ConfigRoute> routes) {
        this.routes = routes;
    }

    public ConfigRoute getRoute(String requestPath) {
        ConfigRoute bestMatch = null;
        int longestMatch = -1;

        for (Map.Entry<String, ConfigRoute> entry : routes.entrySet()) {
            String routePath = entry.getKey();

            if (requestPath.equals(routePath) ||
                    requestPath.startsWith(routePath + "/")) {

                int length = routePath.length();
                if (length > longestMatch) {
                    longestMatch = length;
                    bestMatch = entry.getValue();
                }
            }
        }

        return bestMatch;
    }

}