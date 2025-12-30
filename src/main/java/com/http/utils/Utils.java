package com.http.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.http.config.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

public class Utils {
    public static boolean serveFile(HttpResponse response, String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return false;
            }

            byte[] content = Files.readAllBytes(path);

            // Set body
            response.setBody(content);

            // Set headers
            response.addHeader("Content-Type", MimeTypeResolver.resolve(path));
            response.addHeader("Content-Length", String.valueOf(content.length));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void serveDirectoryListing(HttpRequest request, HttpResponse response, String directory,
            String basePath) {
        Path dirPath = Paths.get(directory);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setBody("Directory not found".getBytes());
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            StringBuilder fileList = new StringBuilder("<html><body><ul>");
            for (Path file : stream) {
                fileList.append("<li><a href=\"").append("http://localhost:"+Integer.toString(request.getPort())) // Adjust host/port if needed
                        .append(basePath).append("/" + file.getFileName()).append("\">").append(file.getFileName())
                        .append("</a></li>");
            }
            fileList.append("</ul></body></html>");
            response.setStatus(HttpStatus.OK);
            response.setBody(fileList.toString().getBytes());
        } catch (IOException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setBody("Failed to list directory".getBytes());
        }
    }

    public static byte[] readFromFile(String path) {
        try {
            Path filePath = Path.of(path);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            return new byte[] {};
        }
    }
}
