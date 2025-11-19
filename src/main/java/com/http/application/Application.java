package com.http.application;

import java.util.ArrayList;
import java.util.List;

import com.http.config.connectors.Connector;

public class Application {
    private List<Connector> connectors;

    public Application(List<Connector> connectors) {
        this.connectors = connectors;
    }

    public Application() {
        this.connectors = new ArrayList<>();
    }

    public void addConnector(Connector connector) {
        this.connectors.add(connector);
    }

    public void start() {
        for (Connector connector : connectors) {
            connector.connect();
        }
    }

    public void stop() {
        for (Connector connector : connectors) {
            connector.disconnect();
        }
    }
}