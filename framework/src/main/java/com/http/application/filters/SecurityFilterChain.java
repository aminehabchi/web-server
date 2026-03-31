package com.http.application.filters;

import java.util.*;

public class SecurityFilterChain {

    private final List<String> permitAllPaths = new ArrayList<>();

    private final List<Integer> excludedPorts = new ArrayList<>();

    public void permitAll(String path) {
        permitAllPaths.add(path);
    }

    public List<String> getPermitAllPaths() {
        return permitAllPaths;
    }

    public void excludedPort(int port) {
        excludedPorts.add(port);
    }
}
