package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Delete;
import com.http.annotation.methodes.Get;
import com.http.annotation.methodes.Post;
import com.http.annotation.parameters.RequestHeader;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Controller
public class helloController {

    @Get(path = "/hello")
    public String GETHelloRequest(HttpRequest req, HttpResponse res, @RequestHeader("User-Agent") String header) {
        // while (true) {
        //     if (req == null) {
        //         break;
        //     }
        // }
        return "get => Hello, World!";
    }

    @Delete(path = "/hello")
    public String DELETEHelloRequest(HttpRequest req, HttpResponse res, @RequestHeader("User-Agent") String header) {
        return "delete => Hello, World!";
    }

    @Post(path = "/hello")
    public String POSTHelloRequest(HttpRequest req, HttpResponse res, @RequestHeader("User-Agent") String header) {
        return "post => Hello, World!";
    }

}