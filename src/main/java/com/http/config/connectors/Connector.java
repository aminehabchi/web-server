package com.http.config.connectors;

import com.http.application.Application;
import com.http.config.defaults.DefaultConnectorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
// import java.nio.channels.AsynchronousServerSocketChannel;
// import java.nio.channels.AsynchronousSocketChannel;
// import java.nio.channels.CompletionHandler;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Connector {
    private int port;
    private long requestTimeout;
    // private AsynchronousServerSocketChannel server;
    private Application application;
    private Selector selector;
    private ServerSocketChannel serverChanel;

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
    // public void connect() {
    // try {
    // server = AsynchronousServerSocketChannel.open()
    // .bind(new InetSocketAddress(port));

    // System.out.println("Server started on port " + port);

    // // Accept connections
    // server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>()
    // {
    // @Override
    // public void completed(AsynchronousSocketChannel client, Void attachment) {
    // try {
    // System.out.println("Client connected: " + client.getRemoteAddress());
    // } catch (IOException ignored) {
    // }

    // // Accept next connection
    // server.accept(null, this);

    // // Handle client reads
    // readFromClient(client);
    // }

    // @Override
    // public void failed(Throwable exc, Void attachment) {
    // System.out.println("Failed to accept connection: " + exc);
    // }
    // });

    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    public void connect() throws IOException {
        selector = Selector.open();
        serverChanel = ServerSocketChannel.open();
        serverChanel.bind(new InetSocketAddress(port));
        serverChanel.configureBlocking(false);
        serverChanel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port " + port);

        while (true) {
            selector.select(1000);
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isAcceptable()) {
                    handleAccept(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                }
            }
            checkTimeouts();
        }
    }

    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            int bytesRead = clientChannel.read(buffer);

            if (bytesRead == -1) {
                clientChannel.close();
                key.cancel();
                System.out.println("Client disconnected.");
                return;
            }

            if (bytesRead > 0) {
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                application.ParseReaquest(this, null, data);
                key.interestOps(SelectionKey.OP_WRITE);
                key.attach(System.currentTimeMillis());
            }

        } catch (IOException e) {
            try {
                clientChannel.close();
                key.cancel();
            } catch (IOException ignored) {
            }
            System.err.println("Read failed: " + e.getMessage());
        }
    }

    private void handleAccept(SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();

            if (clientChannel != null) {
                clientChannel.configureBlocking(false);
                clientChannel.register(selector, SelectionKey.OP_READ);

                // Store connection timestamp for timeout management
                key.attach(System.currentTimeMillis());

                System.out.println("Client connected: " + clientChannel.getRemoteAddress());
            }
        } catch (IOException e) {
            System.err.println("Failed to accept connection: " + e.getMessage());
        }
    }

    private void handleWrite(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        try {
            Object attachment = key.attachment();
            if (attachment instanceof ByteBuffer) {
                ByteBuffer responseBuffer = (ByteBuffer) attachment;

                clientChannel.write(responseBuffer);

                if (!responseBuffer.hasRemaining()) {
                    key.interestOps(SelectionKey.OP_READ);
                    key.attach(System.currentTimeMillis());
                }
            } else {
                key.interestOps(SelectionKey.OP_READ);
            }

        } catch (IOException e) {
            try {
                clientChannel.close();
                key.cancel();
            } catch (IOException ignored) {
            }
            System.err.println("Write failed: " + e.getMessage());
        }
    }

    private void checkTimeouts() {
        long currentTime = System.currentTimeMillis();

        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel) {
                Object attachment = key.attachment();

                if (attachment instanceof Long) {
                    long lastActivity = (Long) attachment;

                    if (currentTime - lastActivity > requestTimeout) {
                        try {
                            System.out.println("Connection timeout, closing client");
                            key.channel().close();
                            key.cancel();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }
    }

    public void writeToClient(SocketChannel client, String msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

        try {
            // Find the selection key for this channel
            for (SelectionKey key : selector.keys()) {
                if (key.channel() == client) {
                    key.attach(buffer); // Store response in attachment
                    key.interestOps(SelectionKey.OP_WRITE);
                    selector.wakeup(); // Wake up selector to process write
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
            System.out.println("Server on port " + port + " stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------------- READ FROM CLIENT ----------------
    // private void readFromClient(AsynchronousSocketChannel client) {
    //     ByteBuffer buffer = ByteBuffer.allocate(256);

    //     client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
    //         @Override
    //         public void completed(Integer bytesRead, ByteBuffer buf) {
    //             if (bytesRead == -1) {
    //                 try {
    //                     client.close();
    //                 } catch (Exception ignored) {
    //                 }
    //                 System.out.println("Client disconnected.");
    //                 return;
    //             }

    //             buf.flip();

    //             application.ParseReaquest(Connector.this, client, buf.array());

    //             buf.clear();
    //             client.read(buf, buf, this);
    //         }

    //         @Override
    //         public void failed(Throwable exc, ByteBuffer buf) {
    //             try {
    //                 client.close();
    //             } catch (Exception ignored) {
    //             }
    //             System.out.println("Read failed: " + exc);
    //         }
    //     });
    // }

    // // ---------------- WRITE TO CLIENT ----------------
    // public void writeToClient(AsynchronousSocketChannel client, String msg) {
    //     ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

    //     client.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
    //         @Override
    //         public void completed(Integer result, ByteBuffer attachment) {
    //             // Write completed
    //         }

    //         @Override
    //         public void failed(Throwable exc, ByteBuffer attachment) {
    //             try {
    //                 client.close();
    //             } catch (Exception ignored) {
    //             }
    //             System.out.println("Write failed: " + exc);
    //         }
    //     });
    // }

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
