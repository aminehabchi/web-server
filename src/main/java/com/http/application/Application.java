package com.http.application;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import com.http.application.context.ApplicationContext;
import com.http.application.dispatcher.Dispatcher;
import com.http.application.filters.FilterChain;
import com.http.application.routing.Router;
import com.http.config.configFile.ConfigurationValidator;
import com.http.config.connectors.Connector;
import com.http.http.request.HttpRequest;
import com.http.http.request.RequestParser;

import com.http.http.response.HttpResponse;

public class Application {
    private Connector connector;
    private ConfigurationValidator configurationValidator;
    private Dispatcher dispatcher;
    private ApplicationContext appContext;
    private FilterChain filaterChain;

    public Application() {
        this.configurationValidator = new ConfigurationValidator();
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
        this.filaterChain = new FilterChain(this.appContext.getFilterBeans());
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
        this.connector = new Connector();
    }

    // ---------------- Connecters ----------------

    public void start() throws Exception {
        connector.setApplication(this);
        connector.setPorts(this.configurationValidator.getPorts());
        connector.connect();
    }

    public void stop() {
        connector.disconnect();
    }

    public void handleClientRequest(Connector connector, SocketChannel client, byte[] requestData) {
        /******************************************
         * Convert raw request data to a String
         ******************************************/
        String result = new String(requestData, StandardCharsets.UTF_8);

        /******************************************
         * Parse the HTTP request
         ******************************************/
        HttpRequest request = null;
        try {
            request = RequestParser.parseRequest(result);
        } catch (Exception e) {
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }
        /******************************************
         * Create a default HTTP response object
         ******************************************/
        HttpResponse response = HttpResponse.defaultResponse();

        /********************************************
         * Check if request is valid based on config
         ********************************************/
        if (!configurationValidator.isValidRequest(request)) {
            response.setBody("not match config");
            writeRsponseToClient(connector, client, response);
            return;
        }

        /********************************************
         * Apply filters
         ********************************************/
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

        /*************************************************************
         * Dispatch the request to the appropriate handler/controller
         *************************************************************/
        try {
            this.dispatcher.dispatch(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            connector.writeToClient(client, "HTTP/1.1 500 Internal Server Error\r\n\r\n");
            return;
        }

        /*************************************************************
         * Send the final response back to the client
         *************************************************************/
        writeRsponseToClient(connector, client, response);
    }

    private void writeRsponseToClient(Connector connector, SocketChannel client, HttpResponse response) {
        response.addHeader("Content-Length", String.valueOf(response.getBody().length()));
        String res = response.toString();
        connector.writeToClient(client, res);
    }
}