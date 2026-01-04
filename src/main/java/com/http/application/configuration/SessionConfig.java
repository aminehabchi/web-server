package com.http.application.configuration;

import com.http.utils.Default;

import lombok.Data;

@Data
public class SessionConfig {

    private String name = Default.DEFAULT_SESSION_NAME;
    private long timeout = Default.DEFAULT_SESSION_TIMEOUT;
    private boolean secure = Default.DEFAULT_SESSION_SECURE;
    private boolean httpOnly = Default.DEFAULT_SESSION_HTTP_ONLY;
    private String path = Default.DEFAULT_SESSION_PATH;
    private String sameSite = Default.DEFAULT_SESSION_SAME_SITE;
}
