package com.http.application.connectors;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.time.Instant;

import com.http.application.connectors.request.ConnectorRequest;

public class ClientContext {
    ConnectorRequest request = new ConnectorRequest();
    Instant now = Instant.now();
    ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    long totalBytesRead = 0;
    boolean complete = false;
    ByteBuffer writeBuffer;

    public ConnectorRequest getRequest() { return request; }
    public void setRequest(ConnectorRequest request) { this.request = request; }

    public Instant getNow() { return now; }
    public void setNow(Instant now) { this.now = now; }

    public ByteBuffer getReadBuffer() { return readBuffer; }
    public void setReadBuffer(ByteBuffer readBuffer) { this.readBuffer = readBuffer; }

    public ByteArrayOutputStream getData() { return data; }
    public void setData(ByteArrayOutputStream data) { this.data = data; }

    public long getTotalBytesRead() { return totalBytesRead; }
    public void setTotalBytesRead(long totalBytesRead) { this.totalBytesRead = totalBytesRead; }

    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }

    public ByteBuffer getWriteBuffer() { return writeBuffer; }
    public void setWriteBuffer(ByteBuffer writeBuffer) { this.writeBuffer = writeBuffer; }
}
