package com.http.application;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

import com.http.application.context.ApplicationContext;
import com.http.application.dispatcher.Dispatcher;
import com.http.application.routing.Router;
import com.http.config.connectors.Connector;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;
import com.http.http.response.ResponseWriter;

public class Application {
    private List<Connector> connectors;
    private Dispatcher dispatcher;
    private ApplicationContext appContext;

    public Application(List<Connector> connectors) {
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
        this.connectors = connectors;

    }

    public Application() {
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
        this.connectors = new ArrayList<>();
    }

    // ---------------- Connecters ----------------
    public void addConnector(Connector connector) {
        this.connectors.add(connector);
    }

    public void start() {
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
    public void ParseReaquest(Connector connector, AsynchronousSocketChannel client, byte[] requestData) {
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
            this.dispatcher.dispatch(request,response);
        } catch (Exception e) {
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        String res = response.toString();
        connector.writeToClient(client, res);
    }

}