package com.http.config.configFile;

import java.util.ArrayList;
import java.util.List;

import com.http.http.request.HttpRequest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConfigurationValidator {
    private List<ConfigServer> servers;
    private List<Integer> ports = new ArrayList<>();;

    public ConfigurationValidator() {
        try {
            this.servers = ConfigurationParser
                    .parse("/home/ahabchi/Desktop/JAVA-LOCALSERVER/src/main/resources/application.properties");
        } catch (Exception e) {
            System.out.println("Error while parsing config file => " + e.getMessage());
            throw e;
        }

        for (ConfigServer s : servers) {
            this.ports.addAll(s.getPorts());
        }

        validatePort(this.ports);
    }

    public List<Integer> getPorts() {
        return ports;
    }

    public void validatePort(List<Integer> ports) {
        for (int port : ports) {
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port number must be between 1 and 65535");
            }
        }
    }

    public boolean isValidRequest(HttpRequest request) {
        try {
            // Check if port exists in configuration
            ConfigServer targetServer = null;
            for (ConfigServer server : servers) {
                if (server.getPorts().contains(request.getPort())) {
                    targetServer = server;
                    break;
                }
            }

            if (targetServer == null) {
                // Port not found
                return false;
            }

            // Optional: check Host header
            String hostHeader = request.getHeaders().getOrDefault("Host", "");
            if (!targetServer.getHosts().contains(hostHeader) && !targetServer.isDefault()) {
                return false;
            }

            // Check body size
            if (request.getBody() != null && request.getBody().length() > targetServer.getClientMaxBodySize()) {
                return false;
            }

            // Optional: check route/method
            // if (targetServer.getRoutes() != null) {
            //     boolean routeValid = targetServer.getRoutes().stream()
            //             .anyMatch(route -> route.getPath().equals(request.getUrl().getPath())
            //                     && route.getMethods().contains(request.getMethod()));
            //     if (!routeValid) {
            //         return false;
            //     }
            // }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
