package com.http.config.url;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class Url {

    private String scheme;
    private String protocol;
    private String authority;
    private String host;
    private String hostname;
    private String subdomain;
    private String domain;
    private String tld;
    private String resource;
    private String directory;
    private String path;
    private String fileName;
    private String fileSuffix;
    private Map<String, String> queryParams = new HashMap<>();

    public Url(String rawUrl) {
        try {
            parse(rawUrl);
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL: " + rawUrl, e);
        }
    }

    private void parse(String rawUrl) throws Exception {
        URL url = new URL(rawUrl);

        // Basic URL components
        this.scheme = url.getProtocol();
        this.protocol = url.getProtocol();
        this.authority = url.getAuthority();
        this.host = url.getHost();
        this.hostname = url.getHost();
        this.resource = url.getFile();
        this.path = url.getPath();

        parseHost(url.getHost());
        parsePath(url.getPath());
        parseQuery(url.getQuery());
    }

    private void parseHost(String host) {
        // IPv6 [2001:db8::1]
        if (host.startsWith("[") && host.endsWith("]")) {
            this.subdomain = "";
            this.domain = host;
            this.tld = "";
            return;
        }

        // localhost
        if (host.equals("localhost")) {
            this.subdomain = "";
            this.domain = "localhost";
            this.tld = "";
            return;
        }

        // IPv4 192.168.1.10
        if (host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            this.subdomain = "";
            this.domain = host;
            this.tld = "";
            return;
        }

        String[] parts = host.split("\\.");

        if (parts.length >= 3) {
            // extract multi-level TLD: com, ma, co.uk, org.ma, etc.
            this.subdomain = parts[0];
            this.domain = parts[parts.length - 2] + "." + parts[parts.length - 1];
            this.tld = parts[parts.length - 1];
        } else if (parts.length == 2) {
            this.subdomain = "";
            this.domain = host;
            this.tld = parts[1];
        } else {
            this.subdomain = "";
            this.domain = host;
            this.tld = "";
        }
    }

    private void parsePath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            directory = "/";
            fileName = "";
            fileSuffix = "";
            return;
        }

        int lastSlash = path.lastIndexOf("/");
        directory = path.substring(0, lastSlash + 1);

        fileName = path.substring(lastSlash + 1);

        if (fileName.contains(".")) {
            int dotIndex = fileName.lastIndexOf(".");
            fileSuffix = fileName.substring(dotIndex + 1);
        } else {
            fileSuffix = "";
        }
    }

    private void parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";
            queryParams.put(key, value);
        }
    }

    public String getQueryParam(String key) {
        return queryParams.get(key);
    }
}
