package com.http.application;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import com.http.application.context.ApplicationContext;
import com.http.application.dispatcher.Dispatcher;
import com.http.application.filters.FilterChain;
import com.http.application.routing.Router;
import com.http.config.configFile.ConfigurationValidator;
import com.http.config.connectors.Connector;
import com.http.config.enums.HttpStatus;
import com.http.config.session.SessionManager;
import com.http.http.request.HttpRequest;
import com.http.http.request.RequestParser;

import com.http.http.response.HttpResponse;

public class Application {
    private Connector connector;
    private ConfigurationValidator configurationValidator;
    private Dispatcher dispatcher;
    private ApplicationContext appContext;
    private FilterChain filaterChain;
    private SessionManager sessionManager;

    public Application() {
        this.configurationValidator = new ConfigurationValidator();
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
        this.filaterChain = new FilterChain(this.appContext.getFilterBeans());
        this.sessionManager = new SessionManager();
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
        System.out.println();
        System.out.println();
        System.out.println("handleClientRequest");
        /******************************************
         * Convert raw request data to a String
         ******************************************/
        String result = new String(requestData, StandardCharsets.UTF_8);

        /******************************************
         * Create a default HTTP response object
         ******************************************/
        HttpResponse response = HttpResponse.defaultResponse();

        /******************************************
         * Parse the HTTP request
         ******************************************/
        HttpRequest request = null;
        try {
            request = RequestParser.parseRequest(result);
        } catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setBody(e.getMessage().getBytes());
            writeRsponseToClient(connector, client, request, response);
            return;
        }

        /********************************************
         * Check if request is valid based on config
         ********************************************/
        if (!configurationValidator.isValidRequest(request, response)) {
            writeRsponseToClient(connector, client, request, response);
            return;
        }

        /********************************************
         * Check if path exist in handler
         ********************************************/
        if (this.dispatcher.isRouteExist(request, response)) {
            /********************************************
             * Apply filters
             ********************************************/
            try {
                if (!this.filaterChain.applyFilters(request, response)) {
                    writeRsponseToClient(connector, client, request, response);
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpStatus.FORBIDDEN);
                response.setBody("Error in filter".getBytes());
                writeRsponseToClient(connector, client, request, response);
                return;
            }

            /*************************************************************
             * Dispatch the request to the appropriate handler/controller
             *************************************************************/
            try {
                this.dispatcher.dispatch(request, response);
            } catch (Exception e) {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                response.setBody(e.getMessage().getBytes());
                writeRsponseToClient(connector, client, request, response);
                return;
            }

        }

        /*************************************************************
         * serve error page based on configuration
         *************************************************************/
        configurationValidator.checkErrorPage(request, response);

        /*************************************************************
         * Send the final response back to the client
         *************************************************************/
        writeRsponseToClient(connector, client, request, response);
    }

    private void writeRsponseToClient(Connector connector, SocketChannel client, HttpRequest request,
            HttpResponse response) {
                
        sessionManager.applySession(request, response);

        if (response.getHeaders().get("Content-Length") != null) {
            response.addHeader("Content-Length", String.valueOf(response.getBody().length));
        }

        byte[] res = response.toBytes();
        connector.writeToClient(client, res);
    }
}