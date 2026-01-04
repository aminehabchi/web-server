package com.http.application;

import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import com.http.application.configuration.Config;
import com.http.application.configuration.ConfigLoader;
import com.http.application.connectors.Connector;
import com.http.application.context.ApplicationContext;
import com.http.application.dispatcher.Dispatcher;
import com.http.application.filters.FilterChain;
import com.http.application.routing.Router;
import com.http.application.session.SessionManager;
import com.http.http.enums.HttpConnection;
import com.http.http.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.request.RequestParser;

import com.http.http.response.HttpResponse;
import com.http.utils.Default;

public class Application {
    private Connector connector;
    private Dispatcher dispatcher;
    private ApplicationContext appContext;
    private FilterChain filaterChain;
    private SessionManager sessionManager;
    private Config config;

    public Application() {
        this.config = ConfigLoader.load(Default.CONFIG_PATH);
        this.appContext = new ApplicationContext();
        this.dispatcher = new Dispatcher(new Router(this.appContext.getControllerBeans()));
        this.filaterChain = new FilterChain(this.appContext.getFilterBeans());
        this.sessionManager = new SessionManager();
        this.connector = new Connector();
    }

    // ---------------- Connecters ----------------
    public void start() throws Exception {
        connector.setApplication(this);
        connector.setPorts(this.config.getPorts());
        connector.connect();
    }

    public void stop() {
        connector.disconnect();
    }

    public void handleClientRequest(Connector connector, SocketChannel client, byte[] requestData,
            String tempBodyfilePath) {
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
            request = RequestParser.parseRequest(result, tempBodyfilePath);
            request.setTempBodyfilePath(tempBodyfilePath);
        } catch (Exception e) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setBody(e.getMessage().getBytes());
            writeRsponseToClient(connector, client, request, response);
            return;
        }

        /******************************************
         * Add Session to request
         ******************************************/
        sessionManager.addSessionToRequest(request);

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

        /*************************************************************
         * Send the final response back to the client
         *************************************************************/
        writeRsponseToClient(connector, client, request, response);
    }

    private void writeRsponseToClient(Connector connector, SocketChannel client, HttpRequest request,
            HttpResponse response) {
        if (request == null) {
            byte[] res = response.toBytes();
            connector.writeToClient(client, res, HttpConnection.CLOSE);
            return;
        }

        /*************************************************************
         * Apply session management
         **************************************************************/
        sessionManager.applySession(request, response);

        /*************************************************************
         * Set Content-Length header if not already set
         *************************************************************/
        response.addContentLengthHeader();

        /*************************************************************
         * Write response back to client
         *************************************************************/
        if (response.isProssesse()) {
            response.setClient(client);
            response.setConnection(request.getConnection());
            response.setConnection(request.getConnection());
            connector.registerPendingCgiResponse(response);
        } else {
            byte[] res = response.toBytes();
            connector.writeToClient(client, res, request.getConnection());
        }
    }
}
