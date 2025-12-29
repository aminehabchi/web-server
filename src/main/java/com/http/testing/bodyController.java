package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;
import com.http.http.request.HttpRequest;

@Controller
public class bodyController {
    @Get(path = "/body-test")
    public String getBody(HttpRequest req) {
        return req.getBodyAsString();
    }

}
