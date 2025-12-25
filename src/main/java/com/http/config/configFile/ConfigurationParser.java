package com.http.config.configFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigurationParser {

    public static List<ConfigServer> parse(String path) {
        List<ConfigServer> servers = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            Map<String, String> props = new HashMap<>();

            // remove comments and empty lines
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf("=");
                if (idx > 0) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    props.put(key, value);
                }
            }

            // Determine server indices
            Set<String> serverKeys = new HashSet<>();
            for (String key : props.keySet()) {
                if (key.startsWith("server.")) {
                    String[] parts = key.split("\\.");
                    if (parts.length > 1) serverKeys.add(parts[1]); // e.g., "1", "2"
                }
            }

            for (String serverId : serverKeys) {
                ConfigServer server = new ConfigServer();

                // Hosts
                String hostsKey = "server." + serverId + ".hosts";
                if (props.containsKey(hostsKey)) {
                    server.setHosts(Arrays.asList(props.get(hostsKey).split(",")));
                }

                // Ports
                String portsKey = "server." + serverId + ".ports";
                if (props.containsKey(portsKey)) {
                    List<Integer> ports = new ArrayList<>();
                    for (String p : props.get(portsKey).split(",")) {
                        ports.add(Integer.parseInt(p));
                    }
                    server.setPorts(ports);
                }

                // Default
                String defaultKey = "server." + serverId + ".default";
                if (props.containsKey(defaultKey)) {
                    server.setDefault(Boolean.parseBoolean(props.get(defaultKey)));
                }

                // Client Max Body Size
                String sizeKey = "server." + serverId + ".clientMaxBodySize";
                if (props.containsKey(sizeKey)) {
                    server.setClientMaxBodySize(Long.parseLong(props.get(sizeKey)));
                }

                // Error pages
                Map<Integer, String> errorPages = new HashMap<>();
                for (String key : props.keySet()) {
                    String prefix = "server." + serverId + ".errorPages.";
                    if (key.startsWith(prefix)) {
                        int code = Integer.parseInt(key.substring(prefix.length()));
                        errorPages.put(code, props.get(key));
                    }
                }
                server.setErrorPages(errorPages);

                // Routes
                List<ConfigRoute> routes = new ArrayList<>();
                int routeIndex = 1;
                while (true) {
                    String routePathKey = "server." + serverId + ".route." + routeIndex + ".path";
                    if (!props.containsKey(routePathKey)) break;

                    ConfigRoute route = new ConfigRoute();
                    route.setPath(props.get(routePathKey));

                    String methodsKey = "server." + serverId + ".route." + routeIndex + ".methods";
                    if (props.containsKey(methodsKey)) {
                        route.setMethods(Arrays.asList(props.get(methodsKey).split(",")));
                    }

                    String rootKey = "server." + serverId + ".route." + routeIndex + ".root";
                    if (props.containsKey(rootKey)) {
                        route.setRoot(props.get(rootKey));
                    }

                    String indexFileKey = "server." + serverId + ".route." + routeIndex + ".indexFile";
                    if (props.containsKey(indexFileKey)) {
                        route.setIndexFile(props.get(indexFileKey));
                    }

                    String dirListingKey = "server." + serverId + ".route." + routeIndex + ".directoryListing";
                    if (props.containsKey(dirListingKey)) {
                        route.setDirectoryListing(Boolean.parseBoolean(props.get(dirListingKey)));
                    }

                    // CGI scripts
                    Map<String, String> cgi = new HashMap<>();
                    for (String key : props.keySet()) {
                        String prefix = "server." + serverId + ".route." + routeIndex + ".cgi.";
                        if (key.startsWith(prefix)) {
                            String ext = key.substring(prefix.length());
                            cgi.put(ext, props.get(key));
                        }
                    }
                    route.setCgi(cgi);

                    routes.add(route);
                    routeIndex++;
                }
                server.setRoutes(routes);

                servers.add(server);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return servers;
    }
}
