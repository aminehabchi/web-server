package com.http.http.request;

import com.http.config.enums.HttpMethod;
import com.http.config.url.Url;

import java.util.Arrays;

enum Level {
    LEVEL_START,
    LEVEL_HEADER,
    LEVEL_BODY
}

public class RequestParser {

    public static HttpRequest parseRequest(String rawRequest) {
        String[] lines = rawRequest.split("\r\n");
        Level level = Level.LEVEL_START;
        HttpRequest httpRequest = new HttpRequest();

        StringBuilder bodyBuilder = new StringBuilder();

        for (String line : lines) {
            switch (level) {
                case LEVEL_START -> {
                    // Parse request line: METHOD URL HTTP/VERSION
                    String[] startLineItems = line.split(" ");
                    if (startLineItems.length != 3) return null;

                    httpRequest.setMethod(HttpMethod.fromReason(startLineItems[0]));
                    httpRequest.setUrl(new Url(startLineItems[1]));
                    httpRequest.setHttpVersion(startLineItems[2]);

                    level = Level.LEVEL_HEADER;
                }

                case LEVEL_HEADER -> {
                    if (line.isEmpty()) {
                        // Empty line means body starts next
                        level = Level.LEVEL_BODY;
                        continue;
                    }

                    String[] headerParts = line.split(":", 2);
                    if (headerParts.length < 2) continue;

                    String key = headerParts[0].trim();
                    String value = headerParts[1].trim();

                    // Special case: Host header may include port
                    if (key.equalsIgnoreCase("Host") && value.contains(":")) {
                        String[] hostParts = value.split(":");
                        value = hostParts[0];
                        httpRequest.setPort(Integer.parseInt(hostParts[1]));
                    }

                    httpRequest.getHeaders().put(key, value);

                    // Parse cookies if header is Cookie
                    if (key.equalsIgnoreCase("Cookie")) {
                        Arrays.stream(value.split(";"))
                                .map(String::trim)
                                .map(s -> s.split("=", 2))
                                .forEach(pair -> {
                                    if (pair.length == 2)
                                        httpRequest.getCookies().put(pair[0], pair[1]);
                                });
                    }

                    // Parse Authorization header
                    if (key.equalsIgnoreCase("Authorization")) {
                        httpRequest.setAuthorization(value);
                    }
                }

                case LEVEL_BODY -> {
                    bodyBuilder.append(line).append("\n");
                }
            }
        }

        // Set body if present
        if (bodyBuilder.length() > 0) {
            httpRequest.setBody(bodyBuilder.toString().trim());
        }

        return httpRequest;
    }
}
