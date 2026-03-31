package com.http.application.session;

import java.util.HashMap;
import java.util.Map;

import com.http.http.cookie.Cookie;

public class Session {
    private final String id;
    private long lastAccessed;
    private final long sessionTimeout;
    private final Map<String, Object> data = new HashMap<>();
    private boolean isRemoved = false;

    public Session(String id, long sessionTimeout) {
        this.id = id;
        this.lastAccessed = System.currentTimeMillis();
        this.sessionTimeout = sessionTimeout;
    }

    public String getId() {
        return id;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }

    public void updateLastAccessed() {
        this.lastAccessed = System.currentTimeMillis();
    }

    public Object getAttribute(String key) {
        return data.get(key);
    }

    public void setAttribute(String key, Object value) {
        data.put(key, value);
    }

    public void removeAttribute(String key) {
        data.remove(key);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - lastAccessed > sessionTimeout;
    }

    public Cookie toCookie() {
        int maxAge = (int) (sessionTimeout);
        return new Cookie("SESSION_ID", this.id, "/", maxAge, false, false);
    }

    public void clear() {
        data.clear();
        isRemoved = true;
    }

    public boolean isRemoved() {
        return isRemoved;
    }
}
