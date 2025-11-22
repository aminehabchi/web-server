package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;
import com.http.annotation.parameters.RequestHeader;
import com.http.annotation.parameters.RequestParam;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Controller
public class helloController {

    @Get(path = "/hello")
    public String handleRequest(HttpRequest req, HttpResponse res, @RequestParam("page") int page,
            @RequestParam("sort") String sort, @RequestHeader("User-Agent") String header) {
        // System.out.println("hello  from  hello");
        // System.out.println("this is page => " + page);
        // System.out.println("this is sort => " + sort);
        // System.out.println("this is header => " + header);
        // System.out.println("is request exist => " + (req != null));
        // System.out.println("is response exist => " + (res != null));
        // System.out.println();
        return "Hello, World!";
    }
}