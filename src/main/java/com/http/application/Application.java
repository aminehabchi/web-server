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
    public void handleClientRequest(Connector connector, SocketChannel client, byte[] requestData) {
        String result = new String(requestData, StandardCharsets.UTF_8);
        System.out.println(result);
        HttpRequest request = null;
        try {
            // Parse the request
            // request = RequestParser.parseRequest(requestData.toString());
            request = HttpRequest.defaultRequest();
        } catch (Exception e) {
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        HttpResponse response = HttpResponse.defaultResponse();

        try {
            if (!this.filaterChain.applyFilters(request, response)) {
                writeRsponseToClient(connector, client, response);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        try {
            this.dispatcher.dispatch(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        writeRsponseToClient(connector, client, response);
    }

    private void writeRsponseToClient(Connector connector, SocketChannel client, HttpResponse response) {
        response.addHeader("Content-Length", String.valueOf(response.getBody().length()));
        String res = response.toString();
        connector.writeToClient(client, res);
    }
}