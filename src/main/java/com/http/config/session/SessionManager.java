package com.http.config.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.http.config.cookie.Cookie;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

public class SessionManager {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final String sessionName = "SESSION_ID";

    public SessionManager() {
    }

    // Create a new session
    public Session createSession(long sessionTimeout) {
        String id = UUID.randomUUID().toString();
        Session session = new Session(id, sessionTimeout);
        sessions.put(id, session);
        return session;
    }

    // Get existing session by ID
    public Session getSession(String id) {
        Session session = sessions.get(id);
        if (session == null)
            return null;

        // Check expiration using Session's own timeout
        if (session.isExpired()) {
            sessions.remove(id);
            return null;
        }

        session.updateLastAccessed();
        return session;
    }

    // Remove a session
    public void removeSession(String id) {
        sessions.remove(id);
    }

    public void cleanup() {
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public void applySession(HttpRequest request, HttpResponse response) {
        String sessionId = request.getCookies().get(sessionName);
        Session session = null;

        System.out.println(sessionId);
        if (sessionId != null) {
            session = getSession(sessionId);
        }

        if (session != null && session.isRemoved()) {
            Cookie cookie = new Cookie(sessionName, "", "/", 0, false, false);
            response.addCookie(cookie);
            return;
        }

        // If no session found or expired, create a new one
        if (session == null || session.isExpired()) {
            session = createSession(30 * 60 * 1000); // 30 min default
        } else {
            // Update last accessed timestamp for active session
            session.updateLastAccessed();
            return;
        }

        // Always add the cookie to the response
        response.addCookie(session.toCookie());
    }

    public void addSessionToRequest(HttpRequest request) {
        String sessionId = request.getCookies().get(sessionName);
        if (sessionId != null) {
            Session session = getSession(sessionId);
            request.setSession(session);
        }
    }

}
