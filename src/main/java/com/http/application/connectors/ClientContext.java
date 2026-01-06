package com.http.application.connectors;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.Instant;

import com.http.application.connectors.request.ConnectorRequest;

import lombok.Data;

@Data
public class ClientContext {
    ConnectorRequest request = new ConnectorRequest();
    Instant now = Instant.now();
    ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    long totalBytesRead = 0;
    boolean complete = false;
    ByteBuffer writeBuffer;
}
