package com.http.testing;


import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;
import com.http.annotation.parameters.RequestParam;
import com.http.http.request.HttpRequest;

@Controller
public class QueryController {
    @Get(path = "/allquery")
    public String allQuerys(HttpRequest req) {

        StringBuilder sb = new StringBuilder();
        for (var entry : req.getUrl().getQueryParams().entrySet()) {
            sb.append("Key: ").append(entry.getKey()).append(", Value: ").append(entry.getValue()).append("<br>");
        }
        return sb.toString();
    }

    @Get(path = "/query-name")
    public String singleStringQuery(@RequestParam("name") String name) {
        return "Query Param 'name' has value: " + name;
    }

    @Get(path = "/query-age")
    public String singleIntigerQuery(@RequestParam("age") int name) {
        System.out.println("age: " + name);
        return "Query Param 'age' has value: " + name;
    }

}