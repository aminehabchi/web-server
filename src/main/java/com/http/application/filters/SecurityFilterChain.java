package com.http.application.filters;

import java.util.*;

public class SecurityFilterChain {



    private final List<String> permitAllPaths = new ArrayList<>();

    
    public void permitAll(String path) {
        permitAllPaths.add(path);
    }
    
    public List<String> getPermitAllPaths() {
        return permitAllPaths;
    }
}
