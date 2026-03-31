package com.http.application.configuration;

import com.http.utils.Default;

public class SessionConfig {
    private String name = Default.DEFAULT_SESSION_NAME;
    private long timeout = Default.DEFAULT_SESSION_TIMEOUT;
    private boolean secure = Default.DEFAULT_SESSION_SECURE;
    private boolean httpOnly = Default.DEFAULT_SESSION_HTTP_ONLY;
    private String path = Default.DEFAULT_SESSION_PATH;
    private String sameSite = Default.DEFAULT_SESSION_SAME_SITE;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getTimeout() { return timeout; }
    public void setTimeout(long timeout) { this.timeout = timeout; }

    public boolean isSecure() { return secure; }
    public void setSecure(boolean secure) { this.secure = secure; }

    public boolean isHttpOnly() { return httpOnly; }
    public void setHttpOnly(boolean httpOnly) { this.httpOnly = httpOnly; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getSameSite() { return sameSite; }
    public void setSameSite(String sameSite) { this.sameSite = sameSite; }
}
