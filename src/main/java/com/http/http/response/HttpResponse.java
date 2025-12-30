package com.http.http.response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.http.config.cookie.Cookie;
import com.http.config.enums.HttpConnection;
import com.http.config.enums.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpResponse {

    private String version;
    private HttpStatus status;
    private Map<String, String> headers = new HashMap<>();
    private List<Cookie> cookies = new ArrayList<>();
    private byte[] body;
    private HttpConnection connection = HttpConnection.KEEP_ALIVE;

    SocketChannel client;

    private Process cgiProcess = null;
    private boolean isProssesse = false;
    private InputStream in = null;
    private boolean isFiniched = false;
    byte[] buffer;
    int read;

    public boolean processCgiOutput() {
        if (in == null) {
            isFiniched = true;
            addContentLengthHeader();
            return true;
        }

        try {
            int bytesAvailable = in.available();

            if (bytesAvailable == 0) {
                // No data ready yet
                return false;
            }

            // Read up to buffer size or available bytes
            int toRead = Math.min(buffer.length, bytesAvailable);
            int bytesRead = in.read(buffer, 0, toRead);

            if (bytesRead > 0) {
                // Append read bytes to body
                byte[] newBody = new byte[body.length + bytesRead];
                System.arraycopy(body, 0, newBody, 0, body.length);
                System.arraycopy(buffer, 0, newBody, body.length, bytesRead);
                body = newBody;
            } else if (bytesRead == -1) {
                // End of stream
                isFiniched = true;
            }
            addContentLengthHeader();

            return true; // We read something

        } catch (IOException e) {
            body = "Error reading stream cgi".getBytes(StandardCharsets.UTF_8);
            isFiniched = true;
            addContentLengthHeader();
            return true;
        }
    }

    public void removeHeader(String key) {
        this.headers.remove(key);
    }

    public void addContentLengthHeader() {
        this.removeHeader("Content-Length");
        this.addHeader("Content-Length", String.valueOf(body.length));
    }

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

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();

        // Status line
        sb.append(version).append(" ").append(status.getCode()).append(" ").append(status.getReason()).append("\r\n");

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

    public HttpConnection getConnection() {
        return this.connection;
    }

    public static HttpResponse defaultResponse() {
        HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.OK);
        response.setVersion("HTTP/1.1");
        response.setBody(new byte[0]);
        response.setConnection(HttpConnection.KEEP_ALIVE);
        // default headers
        response.addHeader("Content-Type", "text/html");

        return response;
    }
}
