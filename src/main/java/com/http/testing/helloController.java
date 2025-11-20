package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;

@Controller
public class helloController {

    @Get(path = "/hello")
    public String handleRequest() {
        System.out.println("hello  from  hello  handler");
        return "Hello, World!";
    }
}