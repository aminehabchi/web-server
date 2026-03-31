package com.app.controllers;

import com.app.services.HelloService;
import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;
import com.http.annotation.methodes.Post;
import com.http.annotation.parameters.RequestBody;
import com.http.annotation.parameters.RequestParam;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Controller("/hello")
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @Get("/")
    public String index(@RequestParam("name") String name) {
        return helloService.greet(name != null ? name : "World");
    }

    @Post("/echo")
    public String echo(HttpRequest request, HttpResponse response, @RequestBody String body) {
        response.addHeader("X-Echo", "true");
        return body;
    }
}
