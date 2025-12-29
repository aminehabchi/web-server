package com.http.testing;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Get;
import com.http.config.session.Session;

@Controller
public class TestSession {
    @Get(path = "/session")
    public String getSession(Session session) {
        if (session == null) {
            return "No session found.";
        }

        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 0;
        }
        visitCount++;
        session.setAttribute("visitCount", visitCount);

        if (visitCount == 10) {
            session.clear();
        }

        return "Visit count: " + visitCount;
    }
}
