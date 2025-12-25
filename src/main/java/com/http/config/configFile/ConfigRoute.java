package com.http.config.configFile;

import java.util.List;
import java.util.Map;

public class ConfigRoute {

    public ConfigRoute() {
    }

    private String path; // "/images", "/api"
    private List<String> methods; // ["GET", "POST"]
    private String redirection; // "/new-path" or null
    private String root; // "/var/www/html"
    private String indexFile; // default file for directory
    private Map<String, String> cgi; // {".php": "/usr/bin/php"}
    private boolean directoryListing; // enable/disable
    private String directoryDefaultResponseFile; // e.g., "/var/www/html/index.html"

    // Getters and Setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public String getRedirection() {
        return redirection;
    }

    public void setRedirection(String redirection) {
        this.redirection = redirection;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getIndexFile() {
        return indexFile;
    }

    public void setIndexFile(String indexFile) {
        this.indexFile = indexFile;
    }

    public Map<String, String> getCgi() {
        return cgi;
    }

    public void setCgi(Map<String, String> cgi) {
        this.cgi = cgi;
    }

    public boolean isDirectoryListing() {
        return directoryListing;
    }

    public void setDirectoryListing(boolean directoryListing) {
        this.directoryListing = directoryListing;
    }

    public String getDirectoryDefaultResponseFile() {
        return directoryDefaultResponseFile;
    }

    public void setDirectoryDefaultResponseFile(String directoryDefaultResponseFile) {
        this.directoryDefaultResponseFile = directoryDefaultResponseFile;
    }
}