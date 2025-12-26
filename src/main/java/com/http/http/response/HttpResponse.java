package com.http.http.response;

import java.nio.charset.StandardCharsets;
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
    private byte[] body;

    public HttpResponse() {
        this.version = "HTTP/1.1";
        this.status = HttpStatus.OK; // default 200 OK
        this.body = new byte[0];

    }

    public HttpResponse(HttpStatus status, byte[] body) {
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

    public byte[] getBody() {
        return body;
    }

    // Setters
    public void setVersion(String version) {
        this.version = version;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();

        // Status line
        sb.append(version).append(" ").append(status.getCode())
                .append(" ").append(status.getReason()).append("\r\n");

        // Headers
        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        // Cookies
        for (Cookie cookie : cookies) {
            sb.append("Set-Cookie: ").append(cookie.toString()).append("\r\n");
        }

        sb.append("\r\n"); // blank line before body

        // Convert headers to bytes
        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        if (body == null || body.length == 0) {
            return headerBytes;
        }

        // Combine header + body
        byte[] fullResponse = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, fullResponse, 0, headerBytes.length);
        System.arraycopy(body, 0, fullResponse, headerBytes.length, body.length);

        return fullResponse;
    }

    public static HttpResponse defaultResponse() {
        HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.OK);
        response.setVersion("HTTP/1.1");
        response.setBody(new byte[0]);

        // default headers
        response.addHeader("Content-Type", "text/html");

        return response;
    }
}
