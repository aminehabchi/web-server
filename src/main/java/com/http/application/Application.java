package com.http.application;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.http.application.context.ApplicationContext;
import com.http.application.dispatcher.Dispatcher;
import com.http.application.filters.FilterChain;
import com.http.application.routing.Router;
import com.http.config.connectors.Connector;
import com.http.http.request.HttpRequest;
import com.http.http.request.RequestParser;

import com.http.http.response.HttpResponse;

public class Application {
    private List<Connector> connectors;
    private Dispatcher dispatcher;
    private ApplicationContext appContext;
    private FilterChain filaterChain;

    public Application(List<Connector> connectors) {
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
        this.filaterChain = new FilterChain(this.appContext.getFilterBeans());
        this.connectors = connectors;
    }

    public Application() {
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
        this.filaterChain = new FilterChain(this.appContext.getFilterBeans());
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

    // ---------------- handle Client Request ----------------
    public void handleClientRequest(Connector connector, AsynchronousSocketChannel client, byte[] requestData) {
        // Step 1: Convert raw request data to a String
        String result = new String(requestData, StandardCharsets.UTF_8);

        HttpRequest request = null;
        try {
            // Step 2: Parse the HTTP request
            request = RequestParser.parseRequest(result);
        } catch (Exception e) {
            // Handle parsing errors and respond with 500 Internal Server Error
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        // Step 3: Create a default HTTP response object
        HttpResponse response = HttpResponse.defaultResponse();

        try {
            // Step 4: Apply filters (e.g., authentication, logging, etc.)
            // If any filter blocks the request, send the response immediately
            if (!this.filaterChain.applyFilters(request, response)) {
                writeRsponseToClient(connector, client, response);
                return;
            }
        } catch (Exception e) {
            // Handle filter errors and respond with 500 Internal Server Error
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        try {
            // Step 5: Dispatch the request to the appropriate handler/controller
            this.dispatcher.dispatch(request, response);
        } catch (Exception e) {
            // Handle dispatch errors and respond with 500 Internal Server Error
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        // Step 6: Send the final response back to the client
        writeRsponseToClient(connector, client, response);
    }

    private void writeRsponseToClient(Connector connector, SocketChannel client, HttpResponse response) {
        response.addHeader("Content-Length", String.valueOf(response.getBody().length()));
        String res = response.toString();
        connector.writeToClient(client, res);
    }
}