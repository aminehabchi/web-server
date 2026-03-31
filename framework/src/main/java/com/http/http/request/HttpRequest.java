package com.http.http.request;

import java.util.HashMap;

import com.http.application.session.Session;
import com.http.http.enums.HttpConnection;
import com.http.http.enums.HttpMethod;
import com.http.http.url.Url;
import com.http.utils.Utils;

public class HttpRequest {
    private int port;
    private String host;

    private HttpMethod method;
    private Url url;

    private String httpVersion;
    private String Authorization;

    private HttpConnection connection = HttpConnection.KEEP_ALIVE;

    private HashMap<String, String> headers = new HashMap<>();
    private HashMap<String, String> cookies = new HashMap<>();
    private HashMap<String, Object> context = new HashMap<>();

    private Session session;

    private String tempBodyfilePath;

    private byte[] body;

    public HttpRequest() {}

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public HttpMethod getMethod() { return method; }
    public void setMethod(HttpMethod method) { this.method = method; }

    public Url getUrl() { return url; }
    public void setUrl(Url url) { this.url = url; }

    public String getHttpVersion() { return httpVersion; }
    public void setHttpVersion(String httpVersion) { this.httpVersion = httpVersion; }

    public String getAuthorization() { return Authorization; }
    public void setAuthorization(String authorization) { this.Authorization = authorization; }

    public HttpConnection getConnection() { return connection; }
    public void setConnection(HttpConnection connection) { this.connection = connection; }

    public HashMap<String, String> getHeaders() { return headers; }
    public void setHeaders(HashMap<String, String> headers) { this.headers = headers; }

    public HashMap<String, String> getCookies() { return cookies; }
    public void setCookies(HashMap<String, String> cookies) { this.cookies = cookies; }

    public HashMap<String, Object> getContext() { return context; }
    public void setContext(HashMap<String, Object> context) { this.context = context; }

    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }

    public String getTempBodyfilePath() { return tempBodyfilePath; }
    public void setTempBodyfilePath(String tempBodyfilePath) { this.tempBodyfilePath = tempBodyfilePath; }

    public byte[] getBody() { return body; }
    public void setBody(byte[] body) { this.body = body; }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public Object getContextAttribute(String key) {
        return context.get(key);
    }

    public void setContextAttribute(String key, Object value) {
        context.put(key, value);
    }

    public String getBodyAsString() {
        if (tempBodyfilePath != null) {
            return new String(Utils.readFromFile(tempBodyfilePath));
        }
        return new String(body);
    }

    public byte[] getBodyAsByte() {
        System.out.println("Temp body file path: " + tempBodyfilePath);
        if (tempBodyfilePath != null) {
            return Utils.readFromFile(tempBodyfilePath);
        }
        return body;
    }
}
