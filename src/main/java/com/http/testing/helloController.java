package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;

@Controller(path = "/api")
public class helloController {

    @Get(path = "/hello")
    public String handleRequest() {
        return "Hello, World!";
    }
}