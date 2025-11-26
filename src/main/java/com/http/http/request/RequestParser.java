package com.http.http.request;

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
        while(index != -1) {
            String line = rawRequest.substring(0, index);
            
            rawRequest = rawRequest.substring(index + target.length());
            index = rawRequest.indexOf(target);
        }
        return null;
    }

    private Level getNextLevel(Level level) {
        switch(level) {
            case Level.LEVEL_START:
                return Level.LEVEL_HEADER;
            case Level.LEVEL_HEADER:
                return Level.LEVEL_BODY;
            case Level.LEVEL_BODY:
                return null;
            default:
                return null;
        }
    }
}
