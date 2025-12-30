package com.http.config.configFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.http.config.enums.HttpMethod;
import com.http.config.enums.HttpStatus;

public class ConfigurationParser {
    
    private static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
        
        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static List<ConfigServer> parse(String path) throws ParseException {
        List<ConfigServer> servers = new ArrayList<>();
        
        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            Map<String, String> props = new HashMap<>();

            // Remove comments & empty lines
            for (String line : lines) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;

                int idx = line.indexOf('=');
                if (idx <= 0)
                    continue;

                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                props.put(key, value);
            }

            // Collect server IDs
            Set<String> serverIds = new HashSet<>();
            for (String key : props.keySet()) {
                if (key.startsWith("server.")) {
                    String[] parts = key.split("\\.");
                    if (parts.length > 1) {
                        serverIds.add(parts[1]);
                    }
                }
            }

            for (String serverId : serverIds) {
                ConfigServer server = new ConfigServer();

                /* ================= HOSTS ================= */
                String hostsKey = "server." + serverId + ".hosts";
                if (props.containsKey(hostsKey)) {
                    Set<String> hosts = new HashSet<>();
                    for (String h : props.get(hostsKey).split(",")) {
                        hosts.add(h.trim());
                    }
                    server.setHosts(hosts);
                }

                /* ================= PORTS ================= */
                String portsKey = "server." + serverId + ".ports";
                if (props.containsKey(portsKey)) {
                    Set<Integer> ports = new HashSet<>();
                    for (String p : props.get(portsKey).split(",")) {
                        ports.add(Integer.parseInt(p.trim()));
                    }
                    server.setPorts(ports);
                }

                /* ================= DEFAULT ================= */
                String defaultKey = "server." + serverId + ".default";
                if (props.containsKey(defaultKey)) {
                    server.setDefault(Boolean.parseBoolean(props.get(defaultKey)));
                }

                /* ================= CLIENT MAX BODY SIZE ================= */
                String sizeKey = "server." + serverId + ".clientMaxBodySize";
                if (props.containsKey(sizeKey)) {
                    server.setClientMaxBodySize(Long.parseLong(props.get(sizeKey)));
                }

                /* ================= ERROR PAGES ================= */
                Map<Integer, String> errorPages = new HashMap<>();
                String errorPrefix = "server." + serverId + ".errorPages.";
                for (String key : props.keySet()) {
                    if (key.startsWith(errorPrefix)) {
                        int code = Integer.parseInt(key.substring(errorPrefix.length()));
                        errorPages.put(code, props.get(key));
                    }
                }
                server.setErrorPages(errorPages);

                /* ================= ROUTES ================= */
                Map<String, ConfigRoute> routes = new HashMap<>();
                int routeIndex = 1;

                while (true) {
                    String pathKey = "server." + serverId + ".route." + routeIndex + ".path";
                    if (!props.containsKey(pathKey))
                        break;

                    ConfigRoute route = new ConfigRoute();
                    String routePath = props.get(pathKey).trim();
                    route.setPath(routePath);

                    /* METHODS */
                    String methodsKey = "server." + serverId + ".route." + routeIndex + ".methods";
                    if (props.containsKey(methodsKey)) {
                        List<HttpMethod> methods = new ArrayList<>();
                        for (String m : props.get(methodsKey).split(",")) {
                            HttpMethod method = HttpMethod.fromReason(m.trim());
                            if (method != null) {
                                methods.add(method);
                            }
                        }
                        route.setMethods(methods);
                    }

                    /* ROOT */
                    String rootKey = "server." + serverId + ".route." + routeIndex + ".root";
                    if (props.containsKey(rootKey)) {
                        route.setRoot(props.get(rootKey));
                    }

                    /* INDEX FILE */
                    String indexKey = "server." + serverId + ".route." + routeIndex + ".indexFile";
                    if (props.containsKey(indexKey)) {
                        route.setIndexFile(props.get(indexKey));
                    }

                    /* DIRECTORY LISTING */
                    String dirKey = "server." + serverId + ".route." + routeIndex + ".directoryListing";
                    if (props.containsKey(dirKey)) {
                        route.setDirectoryListing(Boolean.parseBoolean(props.get(dirKey)));
                    }

                    /* CGI */
                    Map<String, String> cgi = new HashMap<>();
                    String cgiPrefix = "server." + serverId + ".route." + routeIndex + ".cgi.";
                    for (String key : props.keySet()) {
                        if (key.startsWith(cgiPrefix)) {
                            String ext = key.substring(cgiPrefix.length());
                            cgi.put(ext, props.get(key));
                        }
                    }
                    route.setCgi(cgi);

                    /* REDIRECTION */
                    String redirectStatusKey = "server." + serverId + ".route." + routeIndex + ".redirect.status";
                    String redirectToKey = "server." + serverId + ".route." + routeIndex + ".redirect.to";

                    if (props.containsKey(redirectStatusKey) && props.containsKey(redirectToKey)) {
                        String statusStr = props.get(redirectStatusKey);
                        HttpStatus status;

                        try {
                            status = HttpStatus.fromCode(Integer.parseInt(statusStr));
                        } catch (NumberFormatException e) {
                            status = HttpStatus.fromReason(statusStr);
                        }

                        route.setRedirectionStatus(status);
                        route.setRedirection(props.get(redirectToKey));
                    }

                    routes.put(routePath, route);
                    routeIndex++;
                }

                server.setRoutes(routes);
                servers.add(server);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse config file", e);
        }

        return servers;
    }
}