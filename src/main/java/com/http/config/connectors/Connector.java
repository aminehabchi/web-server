package com.http.config.connectors;

import com.http.application.Application;
import com.http.config.defaults.DefaultConnectorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Connector {
    private static final int MAX_HEADER_SIZE = 16 * 1024;
    private static final int MAX_QUEUE_SIZE = 1024;
    private static final int MAX_BODY_SIZE = 5 * 1024 * 1024;
    private static final int MAX_CHUNK_LINE = 2048;
    private static final byte[] HEADER_DELIMITER = new byte[] { '\r', '\n', '\r', '\n' };
    private static final boolean DEBUG_LOG = false;

    private int[] ports = { 8080, 8082 };
    private long requestTimeout;
    private Application application;
    private Selector selector;
    private ServerSocketChannel serverChanel;
    private Queue<RequestTask> globalRequestQueue;

    private static class ConnectionContext {
        boolean isGet = false;
        ByteBuffer readBuffer;
        byte[] headerBytes = new byte[MAX_HEADER_SIZE];
        int headerBytesLen = 0;
        int contentLength = -1;
        int headerEndIndex = -1;
        byte[] bodyBytes = null;
        int bodyBytesReceived = 0;
        boolean chunked = false;
        boolean chunkSizeParsed = false;
        int currentChunkSize = 0;
        int currentChunkRead = 0;
        boolean trailersComplete = false;
        StringBuilder lineBuilder = new StringBuilder();
        boolean pendingCrlfHalf = false;
        ByteArrayOutputStream chunkBuffer = null;
        long lastActivity;
        ByteBuffer writeBuffer = null;
        int writeState = 0;
        Queue<PendingRequest> requestQueue;
        boolean isProcessing = false;

        ConnectionContext() {
            this.readBuffer = ByteBuffer.allocate(8192);
            this.lastActivity = System.currentTimeMillis();
            this.requestQueue = new ConcurrentLinkedQueue<>();
        }

        void reset() {
            readBuffer.clear();
            headerBytesLen = 0;
            contentLength = -1;
            headerEndIndex = -1;
            bodyBytes = null;
            bodyBytesReceived = 0;
            chunked = false;
            chunkSizeParsed = false;
            currentChunkSize = 0;
            currentChunkRead = 0;
            trailersComplete = false;
            lineBuilder.setLength(0);
            pendingCrlfHalf = false;
            chunkBuffer = null;
            writeBuffer = null;
            writeState = 0;
            isProcessing = false;
            requestQueue.clear();
            lastActivity = System.currentTimeMillis();
        }

        void prepareForNextRequest() {
            headerBytesLen = 0;
            contentLength = -1;
            headerEndIndex = -1;
            bodyBytes = null;
            bodyBytesReceived = 0;
            chunked = false;
            chunkSizeParsed = false;
            currentChunkSize = 0;
            currentChunkRead = 0;
            trailersComplete = false;
            lineBuilder.setLength(0);
            pendingCrlfHalf = false;
            chunkBuffer = null;
        }

        void updateActivity() {
            lastActivity = System.currentTimeMillis();
        }

        boolean isHeaderComplete() {
            return headerEndIndex >= 0;
        }
    }

    private static class RequestTask {
        SocketChannel clientChannel;
        byte[] headerBytes;
        int headerLength;
        byte[] bodyBytes;
        SelectionKey selectionKey;

        RequestTask(SocketChannel clientChannel, byte[] headerBytes, int headerLength, byte[] bodyBytes,
                SelectionKey selectionKey) {
            this.clientChannel = clientChannel;
            this.headerBytes = headerBytes;
            this.headerLength = headerLength;
            this.bodyBytes = bodyBytes;
            this.selectionKey = selectionKey;
        }
    }

    private static class PendingRequest {
        byte[] headers;
        int headerLength;
        byte[] bodyBytes;
        long queuedTime;

        PendingRequest(byte[] headers, int headerLength, byte[] bodyBytes) {
            this.headers = headers;
            this.headerLength = headerLength;
            this.bodyBytes = bodyBytes;
            this.queuedTime = System.currentTimeMillis();
        }
    }

    public Connector() {
        // this.port = DefaultConnectorConfig.DEFAULT_PORT;
        this.requestTimeout = DefaultConnectorConfig.DEFAULT_REQUEST_TIMEOUT;
        this.globalRequestQueue = new ConcurrentLinkedQueue<>();
    }

    public Connector(int[] ports) {
        this.validatePort(ports);
        this.ports = ports;
        this.requestTimeout = DefaultConnectorConfig.DEFAULT_REQUEST_TIMEOUT;
        this.globalRequestQueue = new ConcurrentLinkedQueue<>();
    }

    public Connector(long requestTimeout) {
        this.validateRequestTimeout(requestTimeout);
        this.requestTimeout = requestTimeout;
        // this.port = DefaultConnectorConfig.DEFAULT_PORT;
        this.globalRequestQueue = new ConcurrentLinkedQueue<>();
    }

    public Connector(int[] ports, long requestTimeout) {
        this.validatePort(ports);
        this.validateRequestTimeout(requestTimeout);
        this.ports = ports;
        this.requestTimeout = requestTimeout;
        this.globalRequestQueue = new ConcurrentLinkedQueue<>();
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void connect() throws IOException {
        selector = Selector.open();
        for (int port : ports) {
            serverChanel = ServerSocketChannel.open();
            serverChanel.bind(new InetSocketAddress(port));
            serverChanel.configureBlocking(false);
            serverChanel.register(selector, SelectionKey.OP_ACCEPT, port);
            System.out.println("Server started on port " + port);
        }

        while (true) {
            processPendingRequests();

            selector.select(100);
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                }
            }
            checkTimeouts();
        }
    }

    private void processPendingRequests() {
        while (!globalRequestQueue.isEmpty()) {
            RequestTask task = globalRequestQueue.poll();
            if (task != null && task.selectionKey.isValid()) {
                ConnectionContext ctx = (ConnectionContext) task.selectionKey.attachment();
                if (ctx != null) {
                    try {
                        int totalLength = task.headerLength + HEADER_DELIMITER.length + task.bodyBytes.length;
                        byte[] requestBytes = new byte[totalLength];

                        System.arraycopy(task.headerBytes, 0, requestBytes, 0, task.headerLength);
                        System.arraycopy(HEADER_DELIMITER, 0, requestBytes, task.headerLength, HEADER_DELIMITER.length);
                        System.arraycopy(task.bodyBytes, 0, requestBytes, task.headerLength + HEADER_DELIMITER.length,
                                task.bodyBytes.length);

                        if (DEBUG_LOG) {
                            System.out.println(
                                    "=== Processing Request ===\n" + new String(requestBytes, StandardCharsets.UTF_8));
                        }

                        application.handleClientRequest(this, task.clientChannel, requestBytes);
                        ctx.writeState = 1;
                        task.selectionKey.interestOps(SelectionKey.OP_WRITE);
                        selector.wakeup();
                    } catch (Exception e) {
                        System.err.println("Error processing request: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
        if (globalRequestQueue.size() < MAX_QUEUE_SIZE / 2) {
            for (SelectionKey key : selector.keys()) {
                if (!key.isValid()) {
                    continue;
                }
                if (key.channel() instanceof SocketChannel) {
                    ConnectionContext ctx = (ConnectionContext) key.attachment();
                    if (ctx != null && ctx.writeState == 0 && (key.interestOps() & SelectionKey.OP_READ) == 0) {
                        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                    }
                }
            }
        }
    }

    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ConnectionContext ctx = (ConnectionContext) key.attachment();

        if (ctx == null) {
            ctx = new ConnectionContext();
            key.attach(ctx);
        }

        if (DEBUG_LOG) {
            System.out.println("key.interestOps(): " + key.interestOps());
        }

        try {
            if (globalRequestQueue.size() >= MAX_QUEUE_SIZE) {
                key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                return;
            }

            int bytesRead = clientChannel.read(ctx.readBuffer);

            if (bytesRead == -1) {
                if (DEBUG_LOG) {
                    System.out.println("[DEBUG] Closing connection: Client sent EOF");
                }
                clientChannel.close();
                key.cancel();
                return;
            }

            if (bytesRead > 0) {
                ctx.updateActivity();
                ctx.readBuffer.flip();
                while (ctx.readBuffer.hasRemaining()) {
                    if (!ctx.isHeaderComplete()) {
                        while (ctx.readBuffer.hasRemaining() && !ctx.isHeaderComplete()) {
                            if (ctx.headerBytesLen >= MAX_HEADER_SIZE) {
                                if (DEBUG_LOG) {
                                    System.err.println("[DEBUG] Closing connection: Header exceeds max size ("
                                            + MAX_HEADER_SIZE + " bytes)");
                                }
                                // we should create a response that return 400
                                clientChannel.close();
                                key.cancel();
                                return;
                            }

                            byte current = ctx.readBuffer.get();
                            ctx.headerBytes[ctx.headerBytesLen++] = current;

                            if (ctx.headerBytesLen >= 4 &&
                                    ctx.headerBytes[ctx.headerBytesLen - 4] == '\r' &&
                                    ctx.headerBytes[ctx.headerBytesLen - 3] == '\n' &&
                                    ctx.headerBytes[ctx.headerBytesLen - 2] == '\r' &&
                                    ctx.headerBytes[ctx.headerBytesLen - 1] == '\n') {
                                ctx.headerEndIndex = ctx.headerBytesLen - 4;
                            }
                        }

                        if (!ctx.isHeaderComplete()) {
                            ctx.readBuffer.compact();
                            return;
                        }

                        ContentMeta meta = parseContentMeta(ctx.headerBytes, ctx.headerEndIndex);
                        // System.out.println("[DEBUG] meta-length: " + meta.contentLength);
                        // System.out.println("[DEBUG] meta-length: " + meta.);
                        // System.out.println("[DEBUG] meta-length: " + meta.contentLength);

                        if (meta.chunked && meta.contentLength > 0) {
                            clientChannel.close();
                            key.cancel();
                            // 400 error
                            return;
                        }
                        ctx.chunked = meta.chunked;
                        ctx.contentLength = meta.contentLength;
                        ctx.isGet = meta.isGet;
                        if (DEBUG_LOG) {
                            System.out.println("[DEBUG] " + ctx.isGet);
                        }
                        if (!ctx.chunked) {
                            if ((ctx.contentLength < 0 && ctx.isGet == false) || ctx.contentLength > MAX_BODY_SIZE) {
                                if (DEBUG_LOG) {
                                    System.err.println("[DEBUG] Closing connection: Invalid Content-Length ("
                                            + ctx.contentLength + ")");
                                }
                                clientChannel.close();
                                key.cancel();
                                // 400 error
                                return;
                            }
                            ctx.bodyBytes = ctx.contentLength > 0 ? new byte[ctx.contentLength] : new byte[0];
                        } else {
                            ctx.chunkBuffer = new ByteArrayOutputStream();
                        }
                    }
                    if (!ctx.chunked) {
                        if (ctx.contentLength > 0 && ctx.bodyBytesReceived < ctx.contentLength) {
                            int remainingBody = ctx.contentLength - ctx.bodyBytesReceived;
                            int bytesToRead = Math.min(remainingBody, ctx.readBuffer.remaining());
                            ctx.readBuffer.get(ctx.bodyBytes, ctx.bodyBytesReceived, bytesToRead);
                            ctx.bodyBytesReceived += bytesToRead;

                            if (ctx.bodyBytesReceived < ctx.contentLength) {
                                ctx.readBuffer.compact();
                                return;
                            }
                        }
                    } else {
                        if (!accumulateChunkedBody(ctx, clientChannel, key)) {
                            return;
                        }
                    }
                    boolean bodyReady = (!ctx.chunked && ctx.bodyBytesReceived >= Math.max(ctx.contentLength, 0)) ||
                            (ctx.chunked && ctx.trailersComplete);
                    if (ctx.isHeaderComplete() && bodyReady) {
                        if (DEBUG_LOG) {
                            System.out.println("Request complete, queuing for processing...");
                        }
                        byte[] headerCopy = Arrays.copyOf(ctx.headerBytes, ctx.headerEndIndex);
                        byte[] bodyCopy;
                        if (!ctx.chunked) {
                            bodyCopy = Objects.requireNonNullElseGet(ctx.bodyBytes, () -> new byte[0]);
                        } else {
                            bodyCopy = ctx.chunkBuffer != null ? ctx.chunkBuffer.toByteArray() : new byte[0];
                        }
                        globalRequestQueue
                                .offer(new RequestTask(clientChannel, headerCopy, ctx.headerEndIndex, bodyCopy, key));
                        key.interestOps(0);
                        ctx.prepareForNextRequest();
                    }
                }

                ctx.readBuffer.compact();
            }

        } catch (IOException e) {
            try {
                if (DEBUG_LOG) {
                    System.err.println("[DEBUG] Closing connection: IOException during read - " + e.getMessage());
                }
                clientChannel.close();
                key.cancel();
            } catch (IOException ignored) {
            }
        }
    }

    private ContentMeta parseContentMeta(byte[] headerBytes, int headerLength) {
        if (headerBytes == null || headerLength <= 0) {
            return new ContentMeta();
        }
        ContentMeta meta = new ContentMeta();

        String headers = new String(headerBytes, 0, headerLength, StandardCharsets.UTF_8);
        if (DEBUG_LOG) {
            System.out.println("[DEBUG] header: " + headers);
        }

        String[] lines = headers.split("\r\n");
        if (lines[0].startsWith("GET")) {
            meta.isGet = true;
        } else {
            meta.isGet = false;
        }
        for (String line : lines) {
            String lower = line.toLowerCase();
            if (DEBUG_LOG) {
                System.out.println("++ " + lower);
            }
            if (lower.startsWith("content-length:")) {
                try {
                    meta.contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
                } catch (NumberFormatException e) {
                    meta.contentLength = -1;
                }
            }
            // if (lower.startsWith("get")) {
            // meta.isGet = true;
            // } else {
            // meta.isGet = false;
            // }
            if (lower.startsWith("transfer-encoding:")) {
                if (lower.contains("chunked")) {
                    meta.chunked = true;
                }
            }
        }
        return meta;
    }

    private static class ContentMeta {
        int contentLength = -1;
        boolean chunked = false;
        boolean isGet = false;
    }

    private boolean accumulateChunkedBody(ConnectionContext ctx, SocketChannel clientChannel, SelectionKey key)
            throws IOException {
        while (ctx.readBuffer.hasRemaining()) {
            if (!ctx.chunkSizeParsed) {
                String sizeLine = readLine(ctx, ctx.readBuffer);
                if (sizeLine == null) {
                    ctx.readBuffer.compact();
                    return false;
                }
                sizeLine = sizeLine.trim();
                if (sizeLine.isEmpty()) {
                    continue;
                }
                int size;
                try {
                    size = Integer.parseInt(sizeLine, 16);
                } catch (NumberFormatException e) {
                    clientChannel.close();
                    // we need here a 400 responde
                    key.cancel();
                    return false;
                }
                if (size < 0 || size > MAX_BODY_SIZE) {
                    if (DEBUG_LOG) {
                        System.err.println("[DEBUG] Closing connection: Invalid chunk size " + size);
                    }
                    clientChannel.close();
                    // we need here a 400 responde
                    key.cancel();
                    return false;
                }
                ctx.currentChunkSize = size;
                ctx.currentChunkRead = 0;
                ctx.chunkSizeParsed = true;
                if (ctx.chunkBuffer == null) {
                    ctx.chunkBuffer = new ByteArrayOutputStream();
                }

                if (ctx.chunkBuffer.size() + size > MAX_BODY_SIZE) {
                    if (DEBUG_LOG) {
                        System.err.println("[DEBUG] Closing connection: Chunk buffer overflow");
                    }
                    clientChannel.close();
                    key.cancel();
                    return false;
                }

                if (size == 0) {
                    ctx.chunkSizeParsed = true;
                    ctx.currentChunkSize = 0;
                    ctx.currentChunkRead = 0;
                }
                continue;
            }

            if (ctx.currentChunkSize == 0) {
                String trailerLine = readLine(ctx, ctx.readBuffer);
                if (trailerLine == null) {
                    ctx.readBuffer.compact();
                    return false;
                }
                if (trailerLine.isEmpty()) {
                    ctx.trailersComplete = true;
                    ctx.chunkSizeParsed = false;
                    return true;
                }
                continue;
            }
            if (ctx.currentChunkRead < ctx.currentChunkSize) {
                int remainingBody = ctx.currentChunkSize - ctx.currentChunkRead;
                int bytesToRead = Math.min(remainingBody, ctx.readBuffer.remaining());
                if (bytesToRead > 0) {
                    byte[] temp = new byte[bytesToRead];
                    ctx.readBuffer.get(temp);
                    ctx.chunkBuffer.write(temp);
                    ctx.currentChunkRead += bytesToRead;
                }
                if (ctx.currentChunkRead < ctx.currentChunkSize) {
                    ctx.readBuffer.compact();
                    return false;
                }
            }
            if (!consumeChunkCrlf(ctx, clientChannel, key)) {
                return false;
            }
            ctx.chunkSizeParsed = false;
        }
        ctx.readBuffer.compact();
        return false;
    }

    private boolean consumeChunkCrlf(ConnectionContext ctx, SocketChannel channel, SelectionKey key)
            throws IOException {
        if (ctx.readBuffer.remaining() >= 2) {
            byte b1 = ctx.readBuffer.get();
            byte b2 = ctx.readBuffer.get();
            if (b1 == '\r' && b2 == '\n') {
                ctx.pendingCrlfHalf = false;
                return true;
            }
            channel.close();
            key.cancel();
            return false;
        } else if (ctx.readBuffer.remaining() == 1) {
            byte b1 = ctx.readBuffer.get();
            if (b1 == '\r') {
                ctx.pendingCrlfHalf = true;
                ctx.readBuffer.compact();
                return false;
            }
            channel.close();
            key.cancel();
            return false;
        } else {
            ctx.readBuffer.compact();
            return false;
        }
    }

    private String readLine(ConnectionContext ctx, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            if (ctx.pendingCrlfHalf) {
                if (b == '\n') {
                    String line = ctx.lineBuilder.toString();
                    ctx.lineBuilder.setLength(0);
                    ctx.pendingCrlfHalf = false;
                    return line;
                } else {
                    throw new IOException("Invalid CRLF in chunked encoding");
                }
            }

            if (b == '\r') {
                if (buffer.hasRemaining()) {
                    byte next = buffer.get();
                    if (next == '\n') {
                        String line = ctx.lineBuilder.toString();
                        ctx.lineBuilder.setLength(0);
                        return line;
                    } else {
                        throw new IOException("Invalid CRLF in chunked encoding");
                    }
                } else {
                    ctx.pendingCrlfHalf = true;
                    return null;
                }
            } else {
                ctx.lineBuilder.append((char) b);
                if (ctx.lineBuilder.length() > MAX_CHUNK_LINE) {
                    throw new IOException("Chunk line too long");
                }
            }
        }
        return null;
    }

    private void handleAccept(SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();

            if (clientChannel != null) {
                clientChannel.configureBlocking(false);
                SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
                clientKey.attach(new ConnectionContext());

                System.out.println("Client connected: " + clientChannel.getRemoteAddress());
            }
        } catch (IOException e) {
            System.err.println("Failed to accept connection: " + e.getMessage());
        }
    }

    private void handleWrite(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ConnectionContext ctx = (ConnectionContext) key.attachment();

        try {
            if (ctx.writeBuffer != null) {
                int written = clientChannel.write(ctx.writeBuffer);
                ctx.updateActivity();
                if (!ctx.writeBuffer.hasRemaining()) {
                    ctx.reset();
                    key.interestOps(SelectionKey.OP_READ);
                }
            }

        } catch (IOException e) {
            try {
                if (DEBUG_LOG) {
                    System.err.println("[DEBUG] Closing connection: IOException during write - " + e.getMessage());
                }
                clientChannel.close();
                key.cancel();
            } catch (IOException ignored) {
            }
        }
    }

    private void checkTimeouts() {
        long currentTime = System.currentTimeMillis();

        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel && key.isValid()) {
                ConnectionContext ctx = (ConnectionContext) key.attachment();

                if (ctx != null && currentTime - ctx.lastActivity > requestTimeout) {
                    try {
                        if (DEBUG_LOG) {
                            System.out.println(
                                    "[DEBUG] Closing connection: Request timeout (" + requestTimeout + "ms inactive)");
                        }
                        key.channel().close();
                        key.cancel();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    public void writeToClient(SocketChannel client, String msg) {
        try {
            for (SelectionKey key : selector.keys()) {
                if (key.channel() == client && key.isValid()) {
                    ConnectionContext ctx = (ConnectionContext) key.attachment();
                    if (ctx != null) {
                        ctx.writeBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
                        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

                        selector.wakeup();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to queue write: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            if (serverChanel != null && serverChanel.isOpen()) {
                serverChanel.close();
            }
            System.out.println("Server on ports " + ports + " stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] getPort() {
        return ports;
    }

    public void setPort(int[] ports) {
        validatePort(ports);
        this.ports = ports;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        validateRequestTimeout(requestTimeout);
        this.requestTimeout = requestTimeout;
    }

    public void validatePort(int[] ports) {
        for (int port : ports) {
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port number must be between 1 and 65535");
            }
        }
    }

    public void validateRequestTimeout(long requestTimeout) {
        if (requestTimeout < 0) {
            throw new IllegalArgumentException("Request timeout must be non-negative");
        }
    }
}
