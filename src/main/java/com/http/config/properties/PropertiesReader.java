package com.http.config.properties;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    
    private Properties properties;
    
    public PropertiesReader() {
        properties = new Properties();
        loadProperties();
    }
    
    private void loadProperties() {
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
                return;
            }
            
            properties.load(input);
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    

}