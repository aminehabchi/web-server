package com.app;

import com.http.application.Application;

public class Main {
    public static void main(String[] args) throws Exception {
        Application app = new Application("com.app");
        app.start();
    }
}
