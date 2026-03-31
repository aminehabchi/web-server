package com.http.http.response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.http.http.cookie.Cookie;
import com.http.http.enums.HttpConnection;
import com.http.http.enums.HttpStatus;

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

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public HttpStatus getStatus() { return status; }
    public void setStatus(HttpStatus status) { this.status = status; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public List<Cookie> getCookies() { return cookies; }
    public void setCookies(List<Cookie> cookies) { this.cookies = cookies; }

    public byte[] getBody() { return body; }
    public void setBody(byte[] body) { this.body = body; }

    public HttpConnection getConnection() { return connection; }
    public void setConnection(HttpConnection connection) { this.connection = connection; }

    public SocketChannel getClient() { return client; }
    public void setClient(SocketChannel client) { this.client = client; }

    public Process getCgiProcess() { return cgiProcess; }
    public void setCgiProcess(Process cgiProcess) { this.cgiProcess = cgiProcess; }

    public boolean isProssesse() { return isProssesse; }
    public void setProssesse(boolean isProssesse) { this.isProssesse = isProssesse; }

    public InputStream getIn() { return in; }
    public void setIn(InputStream in) { this.in = in; }

    public boolean isFiniched() { return isFiniched; }
    public void setFiniched(boolean isFiniched) { this.isFiniched = isFiniched; }

    public byte[] getBuffer() { return buffer; }
    public void setBuffer(byte[] buffer) { this.buffer = buffer; }

    public int getRead() { return read; }
    public void setRead(int read) { this.read = read; }

    public boolean processCgiOutput() {
        if (in == null) {
            isFiniched = true;
            addContentLengthHeader();
            return true;
        }

        try {
            int bytesAvailable = in.available();

            if (bytesAvailable == 0) {
                return false;
            }

            int toRead = Math.min(buffer.length, bytesAvailable);
            int bytesRead = in.read(buffer, 0, toRead);

            if (bytesRead > 0) {
                byte[] newBody = new byte[body.length + bytesRead];
                System.arraycopy(body, 0, newBody, 0, body.length);
                System.arraycopy(buffer, 0, newBody, body.length, bytesRead);
                body = newBody;
            } else if (bytesRead == -1) {
                isFiniched = true;
            }
            addContentLengthHeader();

            return true;

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
        this.status = HttpStatus.OK;
        this.body = new byte[0];
    }

    public HttpResponse(HttpStatus status, byte[] body) {
        this.version = "HTTP/1.1";
        this.status = status;
        this.body = body;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public byte[] toBytes() {
        StringBuilder sb = new StringBuilder();

        sb.append(version).append(" ").append(status.getCode()).append(" ").append(status.getReason()).append("\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }

        for (Cookie cookie : cookies) {
            sb.append("Set-Cookie: ").append(cookie.toString()).append("\r\n");
        }

        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        if (body == null || body.length == 0) {
            return headerBytes;
        }

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
        response.setConnection(HttpConnection.KEEP_ALIVE);
        response.addHeader("Content-Type", "text/html");
        return response;
    }
}
