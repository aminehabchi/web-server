package com.http.example;

import com.http.application.annotation.classes.Controller;
import com.http.application.annotation.methodes.Get;

@Controller(path = "/api")
public class helloController {

    @Get(path = "/hello")
    public String handleRequest() {
        return "Hello, World!";
    }
}