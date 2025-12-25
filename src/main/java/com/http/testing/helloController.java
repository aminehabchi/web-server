package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;
import com.http.annotation.parameters.RequestHeader;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Controller
public class helloController {

    @Get(path = "/hello")
    public String handleRequest(HttpRequest req, HttpResponse res, @RequestHeader("User-Agent") String header) {
        return "Hello, World!";
    }

}