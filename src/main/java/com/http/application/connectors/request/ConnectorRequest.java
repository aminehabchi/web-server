package com.http.application.connectors.request;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.http.exception.BadRequestException;
import com.http.http.enums.HttpMethod;

import lombok.Data;

enum Level {
    LEVEL_START, LEVEL_HEADER, LEVEL_BODY
}

@Data
public class ConnectorRequest {

    private Level parsingLevel = Level.LEVEL_START;

    private int port;
    private String path;
    private String version;
    private HttpMethod method;

    private byte[] headers;
    private byte[] body;

    private boolean finish = false;

    private int contentLength = 0; // extracted from headers if present
    private ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
    private ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();

    public ConnectorRequest getrequest() {
        return this;
    }

    // Main incremental parse
    public void parse(ByteBuffer buffer) {
        buffer.flip(); // switch to read mode

        while (buffer.hasRemaining() && !finish) {
            switch (parsingLevel) {
                case LEVEL_START -> parseRequestLine(buffer);
                case LEVEL_HEADER -> parseHeaders(buffer);
                case LEVEL_BODY -> parseBody(buffer);
            }
        }

        buffer.compact(); // prepare for next socket read
    }

    private void parseRequestLine(ByteBuffer buffer) {
        StringBuilder line = new StringBuilder();

        while (buffer.hasRemaining()) {
            char c = (char) buffer.get();
            line.append(c);
            if (line.length() >= 2 && line.substring(line.length() - 2).equals("\r\n")) {
                break;
            }
        }

        String[] parts = line.toString().trim().split(" ");
        if (parts.length != 3)
            throw new BadRequestException("Invalid request line: " + line);

        try {
            this.method = HttpMethod.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown HTTP method: " + parts[0]);
        }

        this.path = parts[1];
        this.version = parts[2];

        if (!version.startsWith("HTTP/"))
            throw new BadRequestException("Invalid HTTP version: " + version);

        parsingLevel = Level.LEVEL_HEADER;

        // immediately parse remaining buffer as headers
        if (buffer.hasRemaining()) {
            parseHeaders(buffer);
        }
    }

    private void parseHeaders(ByteBuffer buffer) {
        while (buffer.hasRemaining() && !finish) {
            byte b = buffer.get();
            headerBuffer.write(b);

            if (headerBuffer.size() > 8192)
                throw new BadRequestException("Headers too large");

            int headerEnd = findHeaderEnd(headerBuffer.toByteArray());
            if (headerEnd != -1) {
                headers = slice(headerBuffer.toByteArray(), 0, headerEnd);

                // parse Content-Length
                Map<String, String> headerMap = parseHeadersMap(headers);
                if (headerMap.containsKey("Content-Length")) {
                    try {
                        contentLength = Integer.parseInt(headerMap.get("Content-Length").trim());
                        if (contentLength < 0)
                            throw new BadRequestException("Negative Content-Length");
                    } catch (NumberFormatException e) {
                        throw new BadRequestException("Invalid Content-Length value");
                    }
                }

                // copy any leftover bytes to body buffer
                byte[] allHeaderBytes = headerBuffer.toByteArray();
                if (allHeaderBytes.length > headerEnd) {
                    bodyBuffer.write(allHeaderBytes, headerEnd, allHeaderBytes.length - headerEnd);
                }

                parsingLevel = Level.LEVEL_BODY;

                // If no body expected, mark finish immediately
                if (contentLength == 0) {
                    body = new byte[0];
                    finish = true;
                }
                break;
            }
        }
    }

    private void parseBody(ByteBuffer buffer) {
        if (finish)
            return;

        while (buffer.hasRemaining() && bodyBuffer.size() < contentLength) {
            bodyBuffer.write(buffer.get());
        }

        if (bodyBuffer.size() == contentLength) {
            body = bodyBuffer.toByteArray();
            finish = true; // request fully parsed
        } else if (bodyBuffer.size() > contentLength) {
            throw new BadRequestException("Body larger than Content-Length");
        }
    }

    // helper: find \r\n\r\n
    private int findHeaderEnd(byte[] data) {
        for (int i = 0; i <= data.length - 4; i++) {
            if (data[i] == 13 && data[i + 1] == 10 &&
                    data[i + 2] == 13 && data[i + 3] == 10) {
                return i + 4;
            }
        }
        return -1;
    }

    private Map<String, String> parseHeadersMap(byte[] headerBytes) {
        Map<String, String> map = new HashMap<>();
        String headerStr = new String(headerBytes, StandardCharsets.US_ASCII);
        String[] lines = headerStr.split("\r\n");
        for (String line : lines) {
            int idx = line.indexOf(':');
            if (idx != -1) {
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                map.put(key, value);
            }
        }
        return map;
    }

    private byte[] slice(byte[] data, int start, int end) {
        byte[] result = new byte[end - start];
        System.arraycopy(data, start, result, 0, end - start);
        return result;
    }
}
