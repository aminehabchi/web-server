package com.http;

import com.http.config.properties.PropertiesReader;
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello, World!");
        System.out.println("Maven project is running successfully!");

        System.out.println("-----------");
        PropertiesReader config = new PropertiesReader();

        
        String port = config.getProperty("app.port");
        
      
        System.out.println("Port: " + port);
    }
}