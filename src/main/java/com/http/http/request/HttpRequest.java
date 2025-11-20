package com.http.http.request;

import java.util.HashMap;

import com.http.config.enums.HttpMethod;
import com.http.config.url.Url;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpRequest {

    private HttpMethod method;
    private Url url;

    private String httpVersion;
    private String Authorization;

    private HashMap<String, String> headers;
    private HashMap<String, String> cookies;

    private String Body;

    public HttpRequest() {

    }

   // Static factory method for default HttpRequest
public static HttpRequest defaultRequest() {
    HttpRequest request = new HttpRequest();
    request.setMethod(HttpMethod.GET); // default method

    // Complex URL with path and query parameters
    Url url = new Url("/hello");
    // Suppose your Url class has a method to add parameters
    url.addParameter("page", "1");
    url.addParameter("limit", "20");
    url.addParameter("sort", "name");
    request.setUrl(url);

    request.setHttpVersion("HTTP/1.1"); // default HTTP version
    request.setAuthorization(""); // empty by default

    // Fill headers with common default values
    HashMap<String, String> defaultHeaders = new HashMap<>();
    defaultHeaders.put("Accept", "*/*");
    defaultHeaders.put("User-Agent", "JavaHttpClient/1.0");
    defaultHeaders.put("Content-Type", "application/json");
    request.setHeaders(defaultHeaders);

    // Fill cookies with some example defaults
    HashMap<String, String> defaultCookies = new HashMap<>();
    defaultCookies.put("session_id", "1234567890");
    defaultCookies.put("lang", "en");
    request.setCookies(defaultCookies);

    request.setBody(""); // empty body
    return request;
}

}
