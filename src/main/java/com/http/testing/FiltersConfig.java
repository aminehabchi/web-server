package com.http.testing;

import com.http.annotation.classes.FilterChainConfig;
import com.http.config.Configuration;

@FilterChainConfig
public class FiltersConfig implements Configuration {
    public String configure() {
        return null;
    }
}
