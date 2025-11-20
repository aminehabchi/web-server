package com.http.http.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.http.config.cookie.Cookie;
import com.http.config.enums.HttpStatus;

public class HttpResponse {

    private String version;
    private HttpStatus status;
    private Map<String, String> headers = new HashMap<>();
    private List<Cookie> cookies = new ArrayList<>();
    private String body;

    public HttpResponse() {
        this.version = "HTTP/1.1";
        this.status = HttpStatus.OK; // default 200 OK
        this.body = "";
    }

    public HttpResponse(HttpStatus status, String body) {
        this.version = "HTTP/1.1";
        this.status = status;
        this.body = body;
    }

    // Add a header
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    // Add a cookie
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    // Getters
    public String getVersion() {
        return version;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public String getBody() {
        return body;
    }

    // Setters
    public void setVersion(String version) {
        this.version = version;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // Convert to raw HTTP response string
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(version).append(" ").append(status.getCode())
                .append(" ").append(status.getReason()).append("\r\n");

        // Add headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        // Add cookies as Set-Cookie headers
        for (Cookie cookie : cookies) {
            sb.append("Set-Cookie: ").append(cookie.toString()).append("\r\n");
        }

        sb.append("\r\n"); // blank line before body

        if (body != null) {
            sb.append(body);
        }

        return sb.toString();
    }

    public static HttpResponse defaultResponse() {
        HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.OK);
        response.setVersion("HTTP/1.1");
        response.setBody("");

        // default headers
        response.addHeader("Content-Type", "text/html");

        response.addHeader("Content-Length", String.valueOf(response.getBody().length()));

        // optional: add a default cookie
        Cookie cookie = new Cookie("session_id", "1234567890");
        response.addCookie(cookie);

        return response;
    }
}
