package com.http.application.configuration;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigLoader {

    public static Config load(String fileName) {
        Properties props = new Properties();

        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(fileName)) {

            if (is == null) {
                throw new RuntimeException("Config file not found: " + fileName);
            }

            props.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config file", e);
        }

        return parse(props);
    }

    private static Config parse(Properties props) {
        Config config = new Config();

        // ===== Server =====
        config.setPorts(parsePorts(props.getProperty("server.ports")));
        config.setMaxBodySize(getLong(props, "server.maxBodySize", 10_485_760));
        config.setConnectionTimeout(getLong(props, "server.connectionTimeout", 5000));
        config.setThreadPool(getInt(props, "server.threadPool", 8));
        config.setMaxConnections(getInt(props, "server.maxConnections", 1000));
        config.setBacklog(getInt(props, "server.backlog", 50));
        config.setDebug(getBoolean(props, "server.debug", false));

        // ===== Session =====
        config.setSessionConfig(parseSession(props));

        // ===== Extra configs =====
        config.setConfigs(parseMap(props, "configs."));

        validate(config);
        return config;
    }

    private static SessionConfig parseSession(Properties props) {
        SessionConfig session = new SessionConfig();

        session.setName(props.getProperty("session.name", "SESSION_ID"));
        session.setTimeout(getLong(props, "session.timeout", 1800));
        session.setSecure(getBoolean(props, "session.secure", false));
        session.setHttpOnly(getBoolean(props, "session.httpOnly", true));
        session.setPath(props.getProperty("session.path", "/"));
        session.setSameSite(props.getProperty("session.sameSite", "Lax"));

        return session;
    }

    private static List<Integer> parsePorts(String value) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException("server.ports is required");
        }

        return Arrays.stream(value.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
    }

    private static int getInt(Properties props, String key, int def) {
        String v = props.getProperty(key);
        return v == null ? def : Integer.parseInt(v);
    }

    private static long getLong(Properties props, String key, long def) {
        String v = props.getProperty(key);
        return v == null ? def : Long.parseLong(v);
    }

    private static boolean getBoolean(Properties props, String key, boolean def) {
        String v = props.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v);
    }

    private static Map<String, String> parseMap(Properties props, String prefix) {
        Map<String, String> map = new HashMap<>();

        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                map.put(key.substring(prefix.length()), props.getProperty(key));
            }
        }
        return map;
    }

    private static void validate(Config config) {

        if (config.getPorts().isEmpty()) {
            throw new IllegalStateException("At least one port must be defined");
        }

        if (config.getThreadPool() <= 0) {
            throw new IllegalStateException("threadPool must be > 0");
        }

        if (config.getMaxConnections() <= 0) {
            throw new IllegalStateException("maxConnections must be > 0");
        }

        if (config.getMaxBodySize() <= 0) {
            throw new IllegalStateException("maxBodySize must be > 0");
        }
    }
}
