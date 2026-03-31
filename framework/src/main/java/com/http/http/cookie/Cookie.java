package com.http.http.cookie;

public class Cookie {

    private String name;
    private String value;
    private String path;
    private Integer maxAge;
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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Integer getMaxAge() { return maxAge; }
    public void setMaxAge(Integer maxAge) { this.maxAge = maxAge; }

    public boolean isHttpOnly() { return httpOnly; }
    public void setHttpOnly(boolean httpOnly) { this.httpOnly = httpOnly; }

    public boolean isSecure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }

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
