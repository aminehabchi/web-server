package com.http.http.request;

import java.util.HashMap;

import com.http.config.enums.HttpConnection;
import com.http.config.enums.HttpMethod;
import com.http.config.session.Session;
import com.http.config.url.Url;
import com.http.utils.Utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
