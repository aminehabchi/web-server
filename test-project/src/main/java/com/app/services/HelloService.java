package com.app.services;

import com.http.annotation.classes.Service;

@Service
public class HelloService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
