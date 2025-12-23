package com.http.config.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ServerConfig {
    public String host;
    public int port;
    public boolean defaultServer; // rename if "default" clashes with Java keyword
    @JsonProperty("max-client-body-size")
    public String maxClientBodySize;
    public Map<String, String> error;
}