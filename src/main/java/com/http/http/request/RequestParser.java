package com.http.http.request;

import com.http.config.enums.HttpMethod;
import com.http.config.url.Url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum Level {
    LEVEL_START, LEVEL_HEADER, LEVEL_BODY
}

public class RequestParser {

    private static final Pattern HOST_PORT_PATTERN = Pattern.compile("^(.*?)(:(\\d+))?$");

    public static HttpRequest parseRequest(String rawRequest, String tempBodyfilePath) throws Exception {
        String[] lines = rawRequest.split("\r\n");
        Level level = Level.LEVEL_START;
        HttpRequest httpRequest = new HttpRequest();
        StringBuilder bodyBuilder = new StringBuilder();

        for (String line : lines) {
            switch (level) {
            case LEVEL_START -> {
                // Parse request line: METHOD URL HTTP/VERSION
                String[] startLineItems = line.split(" ");
                if (startLineItems.length != 3)
                    throw new Exception("Malformed request line");

                HttpMethod method = HttpMethod.fromReason(startLineItems[0]);
                if (method == null)
                    throw new Exception("Unsupported HTTP method");

                httpRequest.setMethod(method);

                try {
                    httpRequest.setUrl(new Url(startLineItems[1]));
                } catch (Exception e) {
                    throw new Exception("Invalid URL");
                }

                httpRequest.setHttpVersion(startLineItems[2]);
                level = Level.LEVEL_HEADER;
            }

            case LEVEL_HEADER -> {
                if (line.isEmpty()) {
                    level = Level.LEVEL_BODY;
                    continue;
                }

                String[] headerParts = line.split(":", 2);
                if (headerParts.length < 2)
                    throw new Exception("Malformed header: " + line);

                String key = headerParts[0].trim();
                String value = headerParts[1].trim();

                if (key.equalsIgnoreCase("Host") && value.isEmpty())
                    throw new Exception("Missing Host header");

                httpRequest.getHeaders().put(key, value);

                // Parse host and optional port
                if (key.equalsIgnoreCase("Host")) {
                    Matcher matcher = HOST_PORT_PATTERN.matcher(value);
                    if (matcher.matches()) {
                        httpRequest.setHost(matcher.group(1));
                        if (matcher.group(3) != null) {
                            try {
                                httpRequest.setPort(Integer.parseInt(matcher.group(3)));
                            } catch (NumberFormatException e) {
                                throw new Exception("Invalid port in Host header");
                            }
                        } else {
                            httpRequest.setPort(80); // default HTTP port
                        }
                    } else {
                        throw new Exception("Malformed Host header");
                    }
                }

                if (key.equalsIgnoreCase("Cookie")) {
                    parseCookies(value, httpRequest);
                }
            }

            case LEVEL_BODY -> {
                if (tempBodyfilePath != null) {
                    httpRequest.setTempBodyfilePath(tempBodyfilePath);
                    break;
                } else {
                    bodyBuilder.append(line).append("\n");
                }
            }
            }
        }

        if (bodyBuilder.length() > 0)
            httpRequest.setBody(bodyBuilder.toString().trim().getBytes());

        if (!httpRequest.getHeaders().containsKey("Host"))
            throw new Exception("Missing Host header");

        return httpRequest;
    }

    private static void parseCookies(String cookieHeader, HttpRequest request) {
        String[] cookies = cookieHeader.split(";");

        for (String cookie : cookies) {
            String[] pair = cookie.trim().split("=", 2);
            if (pair.length == 2) {
                request.getCookies().put(pair[0].trim(), pair[1].trim());
            }
        }
    }

}
