package com.http.config.connectors;

import com.http.application.Application;
import com.http.config.defaults.DefaultConnectorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Connector {
    private int port;
    private long requestTimeout;
    private AsynchronousServerSocketChannel server;
    private Application application;

    public Connector() {
        this.port = DefaultConnectorConfig.DEFAULT_PORT;
        this.requestTimeout = DefaultConnectorConfig.DEFAULT_REQUEST_TIMEOUT;
    }

    public Connector(int port) {
        this.validatePort(port);
        this.port = port;
        this.requestTimeout = DefaultConnectorConfig.DEFAULT_REQUEST_TIMEOUT;
    }

    public Connector(long requestTimeout) {
        this.validateRequestTimeout(requestTimeout);
        this.requestTimeout = requestTimeout;
        this.port = DefaultConnectorConfig.DEFAULT_PORT;
    }

    public Connector(int port, long requestTimeout) {
        this.validatePort(port);
        this.validateRequestTimeout(requestTimeout);
        this.port = port;
        this.requestTimeout = requestTimeout;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    // ---------------- CONNECT ----------------
    public void connect() {
        try {
            server = AsynchronousServerSocketChannel.open()
                    .bind(new InetSocketAddress(port));

            System.out.println("Server started on port " + port);

            // Accept connections
            server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel client, Void attachment) {
                    // try {
                        // System.out.println("Client connected: " + client.getRemoteAddress());
                    // } catch (IOException ignored) {
                    // }

                    // Accept next connection
                    server.accept(null, this);

                    // Handle client reads
                    readFromClient(client);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.out.println("Failed to accept connection: " + exc);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- DISCONNECT ----------------
    public void disconnect() {
        try {
            if (server != null && server.isOpen()) {
                server.close();
                System.out.println("Server on port :" + port + " stopped.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------- READ FROM CLIENT ----------------
    private void readFromClient(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer buf) {
                if (bytesRead == -1) {
                    try {
                        client.close();
                    } catch (Exception ignored) {
                    }
                    System.out.println("Client disconnected.");
                    return;
                }

                buf.flip();

                application.handleClientRequest(Connector.this, client, buf.array());

                buf.clear();
                client.read(buf, buf, this);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buf) {
                try {
                    client.close();
                } catch (Exception ignored) {
                    System.out.println("Read failed: " + exc);
                }
            }
        });
    }

    // ---------------- WRITE TO CLIENT ----------------
    public void writeToClient(AsynchronousSocketChannel client, String msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

        client.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                try {
                    // Ensure all bytes are written
                    if (attachment.hasRemaining()) {
                        client.write(attachment, attachment, this); // continue writing
                    } else {
                        client.close(); // close after writing
                    }
                } catch (Exception e) {
                    System.out.println("Error closing client: " + e);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    client.close();
                } catch (Exception ignored) {
                }
                System.out.println("Write failed: " + exc);
            }
        });
    }

    // ---------------- GETTERS & SETTERS ----------------
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        validatePort(port);
        this.port = port;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        validateRequestTimeout(requestTimeout);
        this.requestTimeout = requestTimeout;
    }

    // ---------------- VALIDATION ----------------
    public void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port number must be between 1 and 65535");
        }
    }

    public void validateRequestTimeout(long requestTimeout) {
        if (requestTimeout < 0) {
            throw new IllegalArgumentException("Request timeout must be non-negative");
        }
    }
}
