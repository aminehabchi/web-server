package com.http.config.configFile;

import java.util.List;
import java.util.Map;

import com.http.config.enums.HttpMethod;
import com.http.config.enums.HttpStatus;

public class ConfigRoute {

    public ConfigRoute() {
    }

    private String path; // "/images", "/api"
    private List<HttpMethod> methods; // ["GET", "POST"]
    private String redirection; // "/new-path" or null
    private HttpStatus redirectionStatus;
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

    public List<HttpMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<HttpMethod> methods) {
        this.methods = methods;
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

    public String getRedirection() {
        return redirection;
    }

    public void setRedirection(String redirection) {
        this.redirection = redirection;
    }

    public HttpStatus getRedirectionStatus() {
        return redirectionStatus;
    }

    public void setRedirectionStatus(HttpStatus status) {
        this.redirectionStatus = status;
    }
}