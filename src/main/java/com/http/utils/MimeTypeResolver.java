package com.http.utils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MimeTypeResolver {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("pdf", "application/pdf");
    }

    public static String resolve(Path path) {
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');

        if (dot == -1) {
            return "application/octet-stream";
        }

        String ext = fileName.substring(dot + 1).toLowerCase();
        return MIME_TYPES.getOrDefault(ext, "application/octet-stream");
    }
}
