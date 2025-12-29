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
            
            // Parse and validate properties
            int lineNumber = 0;
            for (String line : lines) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                int idx = line.indexOf("=");
                if (idx <= 0) {
                    throw new ParseException(
                        String.format("Line %d: Invalid property format. Expected 'key=value'", lineNumber)
                    );
                }
                
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                
                // Validate key is not empty
                if (key.isEmpty()) {
                    throw new ParseException(
                        String.format("Line %d: Property key cannot be empty", lineNumber)
                    );
                }
                
                props.put(key, value);
            }
            
            // Determine server indices
            Set<String> serverKeys = new TreeSet<>(); // TreeSet for ordered processing
            for (String key : props.keySet()) {
                if (key.startsWith("server.")) {
                    String[] parts = key.split("\\.");
                    if (parts.length > 1) {
                        serverKeys.add(parts[1]);
                    }
                }
            }
            
            if (serverKeys.isEmpty()) {
                throw new ParseException("No servers defined in configuration file");
            }
            
            // Parse each server
            for (String serverId : serverKeys) {
                try {
                    ConfigServer server = parseServer(serverId, props);
                    servers.add(server);
                } catch (ParseException e) {
                    throw new ParseException(
                        String.format("Error parsing server.%s: %s", serverId, e.getMessage()),
                        e
                    );
                }
            }
            
        } catch (IOException e) {
            throw new ParseException("Failed to read configuration file: " + path, e);
        }
        
        return servers;
    }
    
    private static ConfigServer parseServer(String serverId, Map<String, String> props) {
        ConfigServer server = new ConfigServer();
        String serverPrefix = "server." + serverId + ".";
        
        // Parse hosts (required)
        String hostsKey = serverPrefix + "hosts";
        if (!props.containsKey(hostsKey)) {
            throw new ParseException("Missing required property: " + hostsKey);
        }
        
        String hostsValue = props.get(hostsKey);
        if (hostsValue == null || hostsValue.trim().isEmpty()) {
            throw new ParseException(hostsKey + " cannot be empty");
        }
        
        List<String> hosts = new ArrayList<>();
        for (String host : hostsValue.split(",")) {
            host = host.trim();
            if (!host.isEmpty()) {
                hosts.add(host);
            }
        }
        
        if (hosts.isEmpty()) {
            throw new ParseException(hostsKey + " must contain at least one valid host");
        }
        server.setHosts(hosts);
        
        // Parse ports (required)
        String portsKey = serverPrefix + "ports";
        if (!props.containsKey(portsKey)) {
            throw new ParseException("Missing required property: " + portsKey);
        }
        
        String portsValue = props.get(portsKey);
        if (portsValue == null || portsValue.trim().isEmpty()) {
            throw new ParseException(portsKey + " cannot be empty");
        }
        
        List<Integer> ports = new ArrayList<>();
        for (String portStr : portsValue.split(",")) {
            portStr = portStr.trim();
            if (!portStr.isEmpty()) {
                try {
                    int port = Integer.parseInt(portStr);
                    if (port < 1 || port > 65535) {
                        throw new ParseException(
                            portsKey + ": Port " + port + " is out of valid range (1-65535)"
                        );
                    }
                    ports.add(port);
                } catch (NumberFormatException e) {
                    throw new ParseException(
                        portsKey + ": Invalid port number '" + portStr + "'",
                        e
                    );
                }
            }
        }
        
        if (ports.isEmpty()) {
            throw new ParseException(portsKey + " must contain at least one valid port");
        }
        server.setPorts(ports);
        
        // Parse default (optional, defaults to false)
        String defaultKey = serverPrefix + "default";
        if (props.containsKey(defaultKey)) {
            server.setDefault(parseBoolean(defaultKey, props.get(defaultKey)));
        }
        
        // Parse clientMaxBodySize (optional)
        String sizeKey = serverPrefix + "clientMaxBodySize";
        if (props.containsKey(sizeKey)) {
            String sizeValue = props.get(sizeKey);
            if (sizeValue == null || sizeValue.trim().isEmpty()) {
                throw new ParseException(sizeKey + " cannot be empty");
            }
            
            try {
                long size = Long.parseLong(sizeValue.trim());
                if (size < 0) {
                    throw new ParseException(
                        sizeKey + ": Value must be non-negative, got " + size
                    );
                }
                server.setClientMaxBodySize(size);
            } catch (NumberFormatException e) {
                throw new ParseException(
                    sizeKey + ": Invalid number '" + sizeValue + "'",
                    e
                );
            }
        }
        
        // Parse sessionTimeout (optional)
        String timeoutKey = serverPrefix + "sessionTimeout";
        if (props.containsKey(timeoutKey)) {
            String timeoutValue = props.get(timeoutKey);
            if (timeoutValue == null || timeoutValue.trim().isEmpty()) {
                throw new ParseException(timeoutKey + " cannot be empty");
            }
            
            try {
                long timeout = Long.parseLong(timeoutValue.trim());
                if (timeout < 0) {
                    throw new ParseException(
                        timeoutKey + ": Value must be non-negative, got " + timeout
                    );
                }
                // Store in server if you have a setter for it
                // server.setSessionTimeout(timeout);
            } catch (NumberFormatException e) {
                throw new ParseException(
                    timeoutKey + ": Invalid number '" + timeoutValue + "'",
                    e
                );
            }
        }
        
        // Parse error pages
        Map<Integer, String> errorPages = new HashMap<>();
        String errorPagesPrefix = serverPrefix + "errorPages.";
        for (String key : props.keySet()) {
            if (key.startsWith(errorPagesPrefix)) {
                String codeStr = key.substring(errorPagesPrefix.length());
                try {
                    int code = Integer.parseInt(codeStr);
                    if (code < 100 || code > 599) {
                        throw new ParseException(
                            key + ": HTTP status code must be between 100 and 599, got " + code
                        );
                    }
                    
                    String pagePath = props.get(key);
                    if (pagePath == null || pagePath.trim().isEmpty()) {
                        throw new ParseException(key + " cannot be empty");
                    }
                    
                    errorPages.put(code, pagePath.trim());
                } catch (NumberFormatException e) {
                    throw new ParseException(
                        key + ": Invalid error code '" + codeStr + "'",
                        e
                    );
                }
            }
        }
        server.setErrorPages(errorPages);
        
        // Parse routes
        Map<String, ConfigRoute> routes = new HashMap<>();
        int routeIndex = 1;
        
        while (true) {
            String routePathKey = serverPrefix + "route." + routeIndex + ".path";
            if (!props.containsKey(routePathKey)) {
                break;
            }
            
            try {
                ConfigRoute route = parseRoute(serverId, routeIndex, props);
                routes.put(route.getPath(), route);
            } catch (ParseException e) {
                throw new ParseException(
                    "route." + routeIndex + ": " + e.getMessage(),
                    e
                );
            }
            
            routeIndex++;
        }
        
        server.setRoutes(routes);
        return server;
    }
    
    private static ConfigRoute parseRoute(String serverId, int routeIndex, Map<String, String> props) {
        String routePrefix = "server." + serverId + ".route." + routeIndex + ".";
        ConfigRoute route = new ConfigRoute();
        
        // Parse path (required)
        String pathKey = routePrefix + "path";
        String routePath = props.get(pathKey);
        if (routePath == null || routePath.trim().isEmpty()) {
            throw new ParseException(pathKey + " cannot be empty");
        }
        route.setPath(routePath.trim());
        
        // Parse methods (optional)
        String methodsKey = routePrefix + "methods";
        if (props.containsKey(methodsKey)) {
            String methodsValue = props.get(methodsKey);
            if (methodsValue == null || methodsValue.trim().isEmpty()) {
                throw new ParseException(methodsKey + " cannot be empty");
            }
            
            List<HttpMethod> methods = new ArrayList<>();
            for (String methodStr : methodsValue.split(",")) {
                methodStr = methodStr.trim();
                if (!methodStr.isEmpty()) {
                    HttpMethod method = HttpMethod.fromReason(methodStr);
                    if (method == null) {
                        throw new ParseException(
                            methodsKey + ": Invalid HTTP method '" + methodStr + "'"
                        );
                    }
                    methods.add(method);
                }
            }
            
            if (methods.isEmpty()) {
                throw new ParseException(methodsKey + " must contain at least one valid method");
            }
            route.setMethods(methods);
        }
        
        // Parse root (optional)
        String rootKey = routePrefix + "root";
        if (props.containsKey(rootKey)) {
            String rootValue = props.get(rootKey);
            if (rootValue == null || rootValue.trim().isEmpty()) {
                throw new ParseException(rootKey + " cannot be empty");
            }
            route.setRoot(rootValue.trim());
        }
        
        // Parse indexFile (optional)
        String indexFileKey = routePrefix + "indexFile";
        if (props.containsKey(indexFileKey)) {
            String indexValue = props.get(indexFileKey);
            if (indexValue == null || indexValue.trim().isEmpty()) {
                throw new ParseException(indexFileKey + " cannot be empty");
            }
            route.setIndexFile(indexValue.trim());
        }
        
        // Parse directoryListing (optional)
        String dirListingKey = routePrefix + "directoryListing";
        if (props.containsKey(dirListingKey)) {
            route.setDirectoryListing(parseBoolean(dirListingKey, props.get(dirListingKey)));
        }
        
        // Parse CGI scripts
        Map<String, String> cgi = new HashMap<>();
        String cgiPrefix = routePrefix + "cgi.";
        for (String key : props.keySet()) {
            if (key.startsWith(cgiPrefix)) {
                String ext = key.substring(cgiPrefix.length());
                if (ext.isEmpty()) {
                    throw new ParseException(key + ": CGI extension cannot be empty");
                }
                
                String interpreter = props.get(key);
                if (interpreter == null || interpreter.trim().isEmpty()) {
                    throw new ParseException(key + " cannot be empty");
                }
                
                cgi.put(ext, interpreter.trim());
            }
        }
        route.setCgi(cgi);
        
        // Parse redirection
        String redirectStatusKey = routePrefix + "redirect.status";
        String redirectToKey = routePrefix + "redirect.to";
        
        boolean hasStatus = props.containsKey(redirectStatusKey);
        boolean hasTo = props.containsKey(redirectToKey);
        
        if (hasStatus || hasTo) {
            if (!hasStatus) {
                throw new ParseException(
                    "Redirect defined but missing " + redirectStatusKey
                );
            }
            if (!hasTo) {
                throw new ParseException(
                    "Redirect defined but missing " + redirectToKey
                );
            }
            
            String statusStr = props.get(redirectStatusKey);
            if (statusStr == null || statusStr.trim().isEmpty()) {
                throw new ParseException(redirectStatusKey + " cannot be empty");
            }
            
            String redirectTo = props.get(redirectToKey);
            if (redirectTo == null || redirectTo.trim().isEmpty()) {
                throw new ParseException(redirectToKey + " cannot be empty");
            }
            
            HttpStatus status;
            try {
                int code = Integer.parseInt(statusStr.trim());
                status = HttpStatus.fromCode(code);
                if (status == null) {
                    throw new ParseException(
                        redirectStatusKey + ": Unknown HTTP status code " + code
                    );
                }
            } catch (NumberFormatException e) {
                status = HttpStatus.fromReason(statusStr.trim());
                if (status == null) {
                    throw new ParseException(
                        redirectStatusKey + ": Invalid HTTP status '" + statusStr + "'",
                        e
                    );
                }
            }
            
            route.setRedirection(redirectTo.trim());
            route.setRedirectionStatus(status);
        }
        
        return route;
    }
    
    private static boolean parseBoolean(String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ParseException(key + " cannot be empty");
        }
        
        String trimmed = value.trim().toLowerCase();
        if (trimmed.equals("true")) {
            return true;
        } else if (trimmed.equals("false")) {
            return false;
        } else {
            throw new ParseException(
                key + ": Invalid boolean value '" + value + "'. Expected 'true' or 'false'"
            );
        }
    }
}