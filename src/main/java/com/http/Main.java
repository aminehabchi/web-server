package com.http;

import java.util.List;

import com.http.application.Application;
import com.http.config.configFile.ConfigParser;
import com.http.config.configFile.ConfigServer;
import com.http.config.connectors.Connector;

public class Main {

    public static void main(String[] args) throws Exception {
        Application app = new Application();
        app.addConnector(new Connector(8080));
        app.addConnector(new Connector(8081));
        app.start();

        try {

            List<ConfigServer> servers = ConfigParser
                    .parse("/home/ahabchi/Desktop/JAVA-LOCALSERVER/src/main/resources/application.properties");

        } catch (Exception e) {
            System.out.println("--------");
        }

        Thread.currentThread().join();
    }
}
