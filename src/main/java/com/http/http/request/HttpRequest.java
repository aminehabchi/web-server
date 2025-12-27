package com.http.http.request;

import java.util.HashMap;

import com.http.config.enums.HttpMethod;
import com.http.config.url.Url;

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

    private HashMap<String, String> headers = new HashMap<>();
    private HashMap<String, String> cookies = new HashMap<>();

    private String Body;

    public String getHeader(String key) {
        return headers.get(key);
    }
}
