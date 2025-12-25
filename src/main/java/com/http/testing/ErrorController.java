package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;
import com.http.config.enums.HttpStatus;
import com.http.http.response.HttpResponse;

@Controller
public class ErrorController {

    // 2xx Success
    @Get(path = "/200")
    public HttpResponse handle200(HttpResponse res) {
        res.setStatus(HttpStatus.OK);
        return res;
    }

    @Get(path = "/201")
    public HttpResponse handle201(HttpResponse res) {
        res.setStatus(HttpStatus.CREATED);
        return res;
    }

    @Get(path = "/204")
    public HttpResponse handle204(HttpResponse res) {
        res.setStatus(HttpStatus.NO_CONTENT);
        return res;
    }

    // 3xx Redirection
    @Get(path = "/301")
    public HttpResponse handle301(HttpResponse res) {
        res.setStatus(HttpStatus.MOVED_PERMANENTLY);
        return res;
    }

    @Get(path = "/302")
    public HttpResponse handle302(HttpResponse res) {
        res.setStatus(HttpStatus.FOUND);
        return res;
    }

    // 4xx Client Errors
    @Get(path = "/400")
    public HttpResponse handle400(HttpResponse res) {
        res.setStatus(HttpStatus.BAD_REQUEST);
        return res;
    }

    @Get(path = "/401")
    public HttpResponse handle401(HttpResponse res) {
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return res;
    }

    @Get(path = "/403")
    public HttpResponse handle403(HttpResponse res) {
        res.setStatus(HttpStatus.FORBIDDEN);
        return res;
    }

    @Get(path = "/404")
    public HttpResponse handle404(HttpResponse res) {
        res.setStatus(HttpStatus.NOT_FOUND);
        return res;
    }

    @Get(path = "/405")
    public HttpResponse handle405(HttpResponse res) {
        res.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
        return res;
    }

    @Get(path = "/413")
    public HttpResponse handle413(HttpResponse res) {
        res.setStatus(HttpStatus.PAYLOAD_TOO_LARGE);
        return res;
    }

    // 5xx Server Errors
    @Get(path = "/500")
    public HttpResponse handle500(HttpResponse res) {
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return res;
    }

    @Get(path = "/501")
    public HttpResponse handle501(HttpResponse res) {
        res.setStatus(HttpStatus.NOT_IMPLEMENTED);
        return res;
    }

    @Get(path = "/502")
    public HttpResponse handle502(HttpResponse res) {
        res.setStatus(HttpStatus.BAD_GATEWAY);
        return res;
    }
}
