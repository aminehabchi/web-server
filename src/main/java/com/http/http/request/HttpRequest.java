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
    private HttpMethod method;
    private Url url;

    private String httpVersion;
    private String Authorization;

    private HashMap<String, String> headers;
    private HashMap<String, String> parameters;
    private HashMap<String, String> cookies;


    private String Body;
    
}
