package com.http.application;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.http.application.context.ApplicationContext;
import com.http.application.dispatcher.Dispatcher;
import com.http.application.routing.Router;
import com.http.config.connectors.Connector;
import com.http.http.request.HttpRequest;
import com.http.http.request.RequestParser;
import com.http.http.response.HttpResponse;
import com.http.http.response.ResponseWriter;

public class Application {
    private List<Connector> connectors;
    private Dispatcher dispatcher;
    private ApplicationContext appContext;

    public Application(List<Connector> connectors) {
        this.connectors = connectors;
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
    }

    public Application() {
        this.connectors = new ArrayList<>();
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
    }

    // ---------------- Connecters ----------------
    public void addConnector(Connector connector) {
        this.connectors.add(connector);
    }

    public void start() throws Exception {
        for (Connector connector : connectors) {
            connector.setApplication(this);
            connector.connect();
        }
    }

    public void stop() {
        for (Connector connector : connectors) {
            connector.disconnect();
        }
    }

    // ---------------- Parser ----------------
    public void ParseReaquest(Connector connector, SocketChannel client, byte[] requestData) {
        HttpRequest request = null;
        try {
            String rawRequest = new String(requestData, java.nio.charset.StandardCharsets.UTF_8);
            System.out.println("=== Raw Request ===\n" + rawRequest + "\n==================");
            request = RequestParser.parseRequest(rawRequest);

        } catch (Exception e) {
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        if (this.dispatcher != null) {
            HttpResponse response = this.dispatcher.dispatch(request);
            String res = ResponseWriter.write(response);
            connector.writeToClient(client, res);
        } else {
            System.out.println("DEBUG: " + client + "\n");
            String body = "Hello, World!\n";

            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
                    "\r\n" +
                    body;

            connector.writeToClient(client, response);
        }
    }
}