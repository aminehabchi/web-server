package com.http.http.request;

import com.http.config.enums.HttpMethod;

enum Level {
    LEVEL_START,
    LEVEL_HEADER,
    LEVEL_BODY
}
public class RequestParser {
    public static HttpRequest parseRequest(String rawRequest) {
        String target = "\r\n";
        int index = rawRequest.indexOf(target);
        Level level = Level.LEVEL_START;
        HttpRequest httpRequest = new HttpRequest();
        while(index != -1) {
            String line = rawRequest.substring(0, index);
            if (line.equals("") && level.equals(Level.LEVEL_HEADER)) {
                level = getNextLevel(level);
            }
            if (level.equals(Level.LEVEL_START)) {
                String[] start_line_items = line.split(" ");
                if (start_line_items.length != 3) {
                    return null;
                }
                System.out.println("start_line_items[0] = " + start_line_items[0]);
                System.out.println("start_line_items[1] = " + start_line_items[1]);
                System.out.println("start_line_items[2] = " + start_line_items[2]);
                httpRequest.setMethod(HttpMethod.fromReason(start_line_items[0]));
                httpRequest.setHttpVersion(start_line_items[2]);
                level = getNextLevel(level);
            } else if(level.equals(Level.LEVEL_HEADER)) {
                String[] header = line.split(":");
                if (header.length < 2 ) {
                    return null;
                }
                String key = header[0].trim();
                String value = header[1].trim();
                if (key.equals("Host")) {
                    value  = value + ":" + header[2];
                }
                System.out.println(String.format("{ key = %s; value = %s }", key, value));
                httpRequest.getHeaders().put(key,value);
            } else {
                System.out.println(String.format("body = { %s }", line));
            }
            rawRequest = rawRequest.substring(index + target.length());
            index = rawRequest.indexOf(target);
            //System.out.println("last line = " + line);
        }
        return httpRequest;
    }

    private static Level getNextLevel(Level level) {
        switch(level) {
            case LEVEL_START:
                return Level.LEVEL_HEADER;
            case LEVEL_HEADER:
                return Level.LEVEL_BODY;
            case LEVEL_BODY:
                return null;
            default:
                return null;
        }
    }
}
