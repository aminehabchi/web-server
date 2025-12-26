package com.http.config.configFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.http.config.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConfigurationValidator {
    private List<ConfigServer> servers;
    private List<Integer> ports = new ArrayList<>();;
    private static final String SOURCE = "./src/main/resources";

    public ConfigurationValidator() {
        try {
            this.servers = ConfigurationParser
                    .parse("./src/main/resources/application.properties");
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

    public boolean isValidRequest(HttpRequest request, HttpResponse response) {
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
                response.setStatus(HttpStatus.NOT_FOUND);
                return false;
            }

            // check Host header
            String hostHeader = request.getHeaders().getOrDefault("Host", "");
            if (!targetServer.getHosts().contains(hostHeader) && !targetServer.isDefault()) {
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setBody("Invalid Host header");
                return false;
            }

            // Check body size
            if (request.getBody() != null && request.getBody().length() > targetServer.getClientMaxBodySize()) {
                response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE);
                response.setBody("Body too large then config");
                return false;
            }

            // check Methots for path
            ConfigRoute route = targetServer.getRoute(request.getUrl().getPath());
            if (route != null && !route.getMethods().contains(request.getMethod())) {
                response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                response.setBody("METHOD NOT ALLOWED");
                return false;
            }

            // check redirection
            if (route != null && route.getRedirection() != null) {
                response.setStatus(route.getRedirectionStatus());
                response.getHeaders().put("Location", route.getRedirection());
                return false;
            }
     
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void checkErrorPage(HttpRequest request, HttpResponse response) {
        ConfigServer targetServer = null;

        // Find target server by port
        for (ConfigServer server : servers) {
            if (server.getPorts().contains(request.getPort())) {
                targetServer = server;
                break;
            }
        }

        if (targetServer == null || targetServer.getErrorPages() == null) {
            return;
        }

        int statusCode = response.getStatus().getCode();

        // Find matching error page
        String pagePath = targetServer.getErrorPages().get(statusCode);
        if (pagePath == null) {
            return;
        }

        try {
            File file = new File(SOURCE + pagePath);

            if (!file.exists()) {
                System.err.println("Error page file not found: " + SOURCE + pagePath);
                return;
            }

            // Read the file fully
            String html = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            // Set response
            response.addHeader("Content-Type", "text/html");
            response.setBody(html);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
