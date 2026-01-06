package com.http.application.connectors;

import com.http.application.Application;
import com.http.application.configuration.Config;
import com.http.http.enums.HttpConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Connector {

    private int[] ports;
    private Application application;
    private Selector selector;
    private ServerSocketChannel serverChanel;
    private Config config;

    public Connector(Config config, Application app) {
        this.application = app;
        this.config = config;
        this.ports = config.getPorts().stream().mapToInt(Integer::intValue).toArray();
    }

    public void connect() throws IOException {
        if (config.isDebug()) {
            System.out.println("[DEBUG][handleRead] key.interestOps(): ");
        }

        selector = Selector.open();
        for (int port : ports) {
            serverChanel = ServerSocketChannel.open();
            serverChanel.bind(new InetSocketAddress(port));
            serverChanel.configureBlocking(false);
            serverChanel.register(selector, SelectionKey.OP_ACCEPT, port);
            System.out.println("Server started on port " + port);
        }

        while (true) {
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

        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientContext conn = (ClientContext) key.attachment();

        int bytesRead = channel.read(conn.readBuffer);

        if (bytesRead >= 0) {
            conn.totalBytesRead += bytesRead;

            conn.readBuffer.flip(); // position = 0, limit = bytesRead
            System.out.println(conn.readBuffer.limit());
            if (!conn.readBuffer.hasRemaining()) {
                System.out.println("Buffer has no data to parse");
            } else {
                System.out.println("parsing ...");
                try {
                    conn.request.parse(conn.readBuffer);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

        } else if (bytesRead == -1) {
            // Client closed connection
            key.cancel();
        }

    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();

        SocketChannel client = server.accept();

        if (client == null)
            return;

        client.configureBlocking(false);

        SelectionKey clientKey = client.register(
                key.selector(),
                SelectionKey.OP_READ);

        clientKey.attach(new ClientContext());

    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientContext conn = (ClientContext) key.attachment();

        channel.write(conn.writeBuffer);

        if (!conn.writeBuffer.hasRemaining()) {
            channel.close();
            key.cancel();
        }
    }

    public void writeToClient(SocketChannel client, byte[] msg, HttpConnection conn) {

    }

    public void disconnect() {
        try {
            if (selector != null && selector.isOpen()) {
                if (config.isDebug()) {
                    System.out.println("[DEBUG][disconnect] closing selector");
                }
                selector.close();
            }
            if (serverChanel != null && serverChanel.isOpen()) {
                if (config.isDebug()) {
                    System.out.println("[DEBUG][disconnect] closing server channel");
                }
                serverChanel.close();
            }
            System.out.println("Server on ports " + ports + " stopped.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
