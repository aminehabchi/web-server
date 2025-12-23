package com.http.config.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
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

    private void parse(String rawUrl) throws MalformedURLException {
        // Add default scheme if missing
        URL url = rawUrl.startsWith("http://") || rawUrl.startsWith("https://")
                ? new URL(rawUrl)
                : new URL("http://localhost" + rawUrl); // fallback host for relative URLs

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

    public void addParameter(String key, String value) {
        queryParams.put(key, value); // overwrite existing
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    private void parseHost(String host) {
        if (host == null || host.isEmpty()) {
            this.subdomain = "";
            this.domain = "";
            this.tld = "";
            return;
        }

        // Handle IP or localhost
        if (host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+") || host.equals("localhost")
                || (host.startsWith("[") && host.endsWith("]"))) {
            this.subdomain = "";
            this.domain = host;
            this.tld = "";
            return;
        }

        String[] parts = host.split("\\.");
        if (parts.length >= 3) {
            this.subdomain = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 2));
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
        directory = lastSlash >= 0 ? path.substring(0, lastSlash + 1) : "/";
        fileName = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;

        if (fileName.contains(".")) {
            int dotIndex = fileName.lastIndexOf(".");
            fileSuffix = fileName.substring(dotIndex + 1);
        } else {
            fileSuffix = "";
        }
    }

    private void parseQuery(String query) {
        if (query == null || query.isEmpty())
            return;

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            queryParams.put(kv[0], kv.length > 1 ? kv[1] : "");
        }
    }

    public String getQueryParam(String key) {
        return queryParams.get(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(path);
        if (!queryParams.isEmpty()) {
            sb.append("?");
            queryParams.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            sb.setLength(sb.length() - 1); // remove trailing &
        }
        return sb.toString();
    }
}
