package com.http;

import com.http.application.Application;
import com.http.config.connectors.Connector;

public class Main {

    public static void main(String[] args) throws Exception {
        Application app = new Application();   
        app.addConnector(new Connector(8080));
        app.addConnector(new Connector(8081));
        app.start();

         Thread.currentThread().join();
    }
}
