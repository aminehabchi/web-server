package com.http.config.configFile;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.http.config.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;
import com.http.utils.Utils;

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
            this.servers = ConfigurationParser.parse("./src/main/resources/application.properties");
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
            String hostHeader = request.getHost();
            if (!targetServer.getHosts().contains(hostHeader) && !targetServer.isDefault()) {
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setBody(("Invalid Host header " + hostHeader).getBytes());
                return false;
            }

            // Check body size
            if (request.getBody() != null && request.getBody().length() > targetServer.getClientMaxBodySize()) {
                response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE);
                response.setBody("Body too large then config".getBytes());
                return false;
            }

            // check Methots for path
            ConfigRoute route = targetServer.getRoute(request.getUrl().getPath());
            if (route != null && !route.getMethods().contains(request.getMethod())) {
                response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
                response.setBody("METHOD NOT ALLOWED".getBytes());
                return false;
            }

            // check redirection
            if (route != null && route.getRedirection() != null) {
                response.setStatus(route.getRedirectionStatus());
                response.getHeaders().put("Location", route.getRedirection());
                response.setBody(new byte[0]);
                return false;
            }

            // handle CGI
            if (!cgiHandler(request, response, route)) {
                return false;
            }

            if (!StaticResourceResolver(request, response, route)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cgiHandler(HttpRequest request, HttpResponse response, ConfigRoute route) {
        if (route == null || route.getCgi() == null || route.getCgi().size()==0) {
            return true;
        }


        String currentPath = request.getUrl().getPath();
        String basePath = route.getPath();
        String cgiName = currentPath.substring(basePath.length());
        String[] ext = cgiName.split("\\.");

        if (ext == null || ext.length != 2) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setBody("Error : CGI file name".getBytes());
            return false;
        }
        String interpreter = route.getCgi().get(ext[1]);
        String cgiPath;
        if (route.getRoot() != null) {
            cgiPath = SOURCE + route.getRoot() + cgiName;
        } else {
            cgiPath = SOURCE + route.getRoot() + cgiName;
        }

        try {
            ProcessBuilder pb;
            if (interpreter != null) {
                pb = new ProcessBuilder(interpreter, cgiPath);
            } else {
                pb = new ProcessBuilder(cgiPath);
            }

            // Set environment variables
            Map<String, String> env = pb.environment();
            env.put("REQUEST_METHOD", request.getMethod().name());
            env.put("QUERY_STRING",
                    request.getUrl().getQueryParams() != null ? request.getUrl().getQueryParams().toString() : "");
            if (request.getBody() != null) {
                env.put("CONTENT_LENGTH", String.valueOf(request.getBody().length()));
            }

            env.put("CONTENT_TYPE", request.getHeader("Content-Type") != null ? request.getHeader("Content-Type") : "");
            env.put("SCRIPT_FILENAME", cgiPath);
            env.put("SCRIPT_NAME", cgiName);
            env.put("SERVER_NAME", request.getHost());
            env.put("SERVER_PORT", String.valueOf(request.getPort()));

            pb.redirectErrorStream(true); // combine stdout and stderr
            Process process = pb.start();

            // Write POST body if needed
            if ("POST".equalsIgnoreCase(request.getMethod().name()) && request.getBody() != null
                    && request.getBody().length() > 0) {
                try (OutputStream os = process.getOutputStream()) {
                    if (request.getBody() != null) {
                        os.write(request.getBody().getBytes()); // write actual body bytes
                        os.flush();
                    }
                }
            }

            // Read CGI output (just the body)
            byte[] output = process.getInputStream().readAllBytes();
            process.waitFor();

            // Set the response body only
            response.setBody(output);

            return false;

        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setBody(("CGI execution error: " + e.getMessage()).getBytes());
        }

        return false;
    }

    public boolean StaticResourceResolver(HttpRequest request, HttpResponse response, ConfigRoute route) {
        if (route == null || route.getRoot() == null) {
            return true;
        }

        String currentPath = request.getUrl().getPath();
        String basePath = route.getPath();

        // If the requested path matches the route base
        if (currentPath.equals(basePath)) {
            if (route.isDirectoryListing()) {
                // Serve directory listing
                Utils.serveDirectoryListing(response, SOURCE + route.getRoot(), basePath);
            } else if (route.getIndexFile() != null) {
                // Serve index file
                String indexPath = SOURCE + route.getRoot() + "/" + route.getIndexFile();
                Utils.serveFile(response, indexPath);
            } else {
                // Directory listing not allowed
                response.setStatus(HttpStatus.FORBIDDEN);
                response.setBody("You are not allowed to list this directory".getBytes());
            }
            return false;
        }

        // Handle files under the route
        String fileName = currentPath.substring(basePath.length());
        String filePath = SOURCE + route.getRoot() + fileName;

        if (!Utils.serveFile(response, filePath)) {
            // Fallback to index file if exists
            if (route.getIndexFile() != null) {
                Utils.serveFile(response, SOURCE + route.getRoot() + "/" + route.getIndexFile());
            } else {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setBody(("File " + fileName + " : Not found").getBytes());
            }
            return false;
        }

        return false;
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
            Utils.serveFile(response, SOURCE + pagePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
