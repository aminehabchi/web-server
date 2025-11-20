package com.http.config.cookie;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cookie {

    private String name;
    private String value;
    private String path;
    private Integer maxAge; // in seconds
    private boolean httpOnly;
    private boolean secure;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie(String name, String value, String path, Integer maxAge, boolean httpOnly, boolean secure) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.maxAge = maxAge;
        this.httpOnly = httpOnly;
        this.secure = secure;
    }


    // Convert to Set-Cookie header string
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);

        if (path != null) sb.append("; Path=").append(path);
        if (maxAge != null) sb.append("; Max-Age=").append(maxAge);
        if (secure) sb.append("; Secure");
        if (httpOnly) sb.append("; HttpOnly");

        return sb.toString();
    }
}
