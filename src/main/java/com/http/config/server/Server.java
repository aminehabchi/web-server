package com.http.config.server;

import java.util.List;
import java.util.Map;

public class Server {

    private List<String> hosts; // e.g., ["127.0.0.1", "0.0.0.0"]
    private List<Integer> ports; // e.g., [80, 8080]
    private boolean isDefault; // default server selection
    private Map<Integer, String> errorPages; // custom error pages {404: "/errors/404.html"}
    private int clientMaxBodySize; // in bytes, e.g., 1048576

    private List<Route> routes;

    // Getters and Setters
    public List<String> getHosts() { return hosts; }
    public void setHosts(List<String> hosts) { this.hosts = hosts; }

    public List<Integer> getPorts() { return ports; }
    public void setPorts(List<Integer> ports) { this.ports = ports; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public Map<Integer, String> getErrorPages() { return errorPages; }
    public void setErrorPages(Map<Integer, String> errorPages) { this.errorPages = errorPages; }

    public int getClientMaxBodySize() { return clientMaxBodySize; }
    public void setClientMaxBodySize(int clientMaxBodySize) { this.clientMaxBodySize = clientMaxBodySize; }

    public List<Route> getRoutes() { return routes; }
    public void setRoutes(List<Route> routes) { this.routes = routes; }

    // Nested Route class
    public static class Route {
        private String path; // "/images", "/api"
        private List<String> methods; // ["GET", "POST"]
        private String redirection; // "/new-path" or null
        private String root; // "/var/www/html"
        private String indexFile; // default file for directory
        private Map<String, String> cgi; // {".php": "/usr/bin/php"}
        private boolean directoryListing; // enable/disable
        private String directoryDefaultResponseFile; // e.g., "/var/www/html/index.html"

        // Getters and Setters
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public List<String> getMethods() { return methods; }
        public void setMethods(List<String> methods) { this.methods = methods; }

        public String getRedirection() { return redirection; }
        public void setRedirection(String redirection) { this.redirection = redirection; }

        public String getRoot() { return root; }
        public void setRoot(String root) { this.root = root; }

        public String getIndexFile() { return indexFile; }
        public void setIndexFile(String indexFile) { this.indexFile = indexFile; }

        public Map<String, String> getCgi() { return cgi; }
        public void setCgi(Map<String, String> cgi) { this.cgi = cgi; }

        public boolean isDirectoryListing() { return directoryListing; }
        public void setDirectoryListing(boolean directoryListing) { this.directoryListing = directoryListing; }

        public String getDirectoryDefaultResponseFile() { return directoryDefaultResponseFile; }
        public void setDirectoryDefaultResponseFile(String directoryDefaultResponseFile) { this.directoryDefaultResponseFile = directoryDefaultResponseFile; }
    }
}
