package com.http.config.configFile;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Set<Integer> ports = new HashSet<>();;
    private static final String SOURCE = "./src/main/resources";

    public ConfigurationValidator() {
        try {
            this.servers = ConfigurationParser.parse("./src/main/resources/application.properties");
        } catch (Exception e) {
            System.out.println("Error while parsing config file => " + e.getMessage());
            throw e;
        }

        for (ConfigServer server : this.servers) {
            this.ports.addAll(server.getPorts());
        }

        validatePort(new ArrayList<>(this.ports));

        validateConfiguration(servers);
    }

    public void validateConfiguration(List<ConfigServer> servers) {
        for (int port : this.ports) {
            // Check for multiple default servers on the same port
            int countDefault = 0;
            for (ConfigServer server : servers) {
                if (server.getPorts().contains(port) && server.isDefault()) {
                    countDefault++;
                }
            }
            if (countDefault > 1) {
                throw new IllegalArgumentException("Multiple default servers configured for port " + port);
            }

            // Check for duplicate hosts on the same port
            Set<String> hostSet = new HashSet<>();
            for (ConfigServer server : servers) {
                if (server.getPorts().contains(port)) {
                    for (String host : server.getHosts()) {
                        if (hostSet.contains(host)) {

                            throw new IllegalArgumentException(
                                    "Duplicate host '" + host + "' configured for port " + port);
                        }
                        hostSet.add(host);

                    }
                }
            }

            // Clear hostSet for the next port
            hostSet.clear();
        }
    }

    public List<Integer> getPorts() {
        return new ArrayList<>(this.ports);
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
            // port && host check
            int port = request.getPort();
            String host = request.getHost();

            // Get target server by port & host
            ConfigServer targetServer = null;
            for (ConfigServer server : servers) {
                if (server.getPorts().contains(port) && server.getHosts().contains(host)) {
                    targetServer = server;
                    break;
                }
            }

            // If no exact match, find default server for the port
            if (targetServer == null) {
                for (ConfigServer server : servers) {
                    if (server.getPorts().contains(port) && server.isDefault()) {
                        targetServer = server;
                        break;
                    }
                }
                if (targetServer == null) {
                    response.setStatus(HttpStatus.NOT_FOUND);
                    response.setBody(("Invalid Host header " + host).getBytes());
                    return false;
                }
            }

            // Check body size
            if (request.getBody() != null && request.getBodyAsByte().length > targetServer.getClientMaxBodySize()) {
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
        if (route == null || route.getCgi() == null || route.getCgi().size() == 0) {
            return true;
        }

        System.out.println("CGI HANDLER");

        String[] extensions = { ".py", ".pl", ".sh" };
        String currentPath = request.getUrl().getPath();
        String basePath = route.getPath(); // e.g., "/cgi-bin"
        String remainingPath = currentPath.substring(basePath.length()); // e.g., "/hello.py/hh/world"

        // Find which extension matches
        String cgiName = null;
        String pathInfo = "";
        for (String ext : extensions) {
            int idx = remainingPath.indexOf(ext);
            if (idx != -1) {
                cgiName = remainingPath.substring(0, idx + ext.length()); // script name
                pathInfo = remainingPath.substring(idx + ext.length()); // PATH_INFO
                break;
            }
        }

        if (cgiName == null) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setBody("No CGI script found!".getBytes());
            return false;
        } else {
            System.out.println("CGI NAME  : " + cgiName); // e.g., /hello.py
            System.out.println("PATH_INFO : " + pathInfo); // e.g., /hh/world
        }
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
                env.put("CONTENT_LENGTH", String.valueOf(request.getBody().length));
            }

            env.put("PATH_INFO", pathInfo);

            env.put("CONTENT_TYPE", request.getHeader("Content-Type") != null ? request.getHeader("Content-Type") : "");
            env.put("SCRIPT_FILENAME", cgiPath);
            env.put("SCRIPT_NAME", cgiName);
            env.put("SERVER_NAME", request.getHost());
            env.put("SERVER_PORT", String.valueOf(request.getPort()));

            pb.redirectErrorStream(true); // combine stdout and stderr
            Process process = pb.start();

            // Write POST body if needed
            if ("POST".equalsIgnoreCase(request.getMethod().name()) && request.getBody() != null
                    && request.getBody().length > 0) {
                try (OutputStream os = process.getOutputStream()) {
                    if (request.getBody() != null) {
                        os.write(request.getBody()); // write actual body bytes
                        os.flush();
                    }
                }
            }

            // Read CGI output (just the body)
            response.setCgiProcess(process);
            response.setIn(process.getInputStream());
            response.setBuffer(new byte[1024]);
            response.setProssesse(true);
            response.setFiniched(false);

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
                Utils.serveDirectoryListing(request,response, SOURCE + route.getRoot(), basePath);
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
