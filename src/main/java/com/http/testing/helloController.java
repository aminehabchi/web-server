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
        System.out.println("hello  from  hello handler");
        // System.out.println("this is page => " + page);
        // System.out.println("this is sort => " + sort);
        // System.out.println("this is header => " + header);
        // System.out.println("is request exist => " + (req != null));
        // System.out.println("is response exist => " + (res != null));
        // System.out.println();
        return "Hello, World!";
    }

    @Get(path = "/hello2")
    public String handleRequest2(HttpRequest req, HttpResponse res, @RequestHeader("User-Agent") String header) {
        System.out.println("hello  from  hello handler");
        // System.out.println("this is page => " + page);
        // System.out.println("this is sort => " + sort);
        // System.out.println("this is header => " + header);
        // System.out.println("is request exist => " + (req != null));
        // System.out.println("is response exist => " + (res != null));
        // System.out.println();
        return "Hello, World 2!";
    }
}