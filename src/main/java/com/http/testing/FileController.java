package com.http.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Post;
import com.http.annotation.parameters.RequestHeader;
import com.http.config.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Controller
public class FileController {

    private static final Map<String, String> allowedTypes = Map.of("text/plain", "txt", "image/png", "png",
            "image/jpeg", "jpg", "application/pdf", "pdf", "video/mp4", "mp4", "video/webm", "webm");

    @Post(path = "/upload")
    public String uploadFile(HttpRequest req, HttpResponse res, @RequestHeader("Content-Type") String header) {

        try {
            // Validate body
            byte[] bodyBytes = req.getBodyAsByte();
            if (bodyBytes == null || bodyBytes.length == 0) {
                res.setStatus(HttpStatus.BAD_REQUEST);
                return "Request body is empty";
            }

            // Validate Content-Type
            if (header == null) {
                res.setStatus(HttpStatus.BAD_REQUEST);
                return "Missing Content-Type header";
            }

            System.err.println("temp file: " + req.getTempBodyfilePath());
            System.out.println("content length: " + bodyBytes.length);

            String contentType = header.split(";")[0].trim().toLowerCase();

            if (!allowedTypes.containsKey(contentType)) {
                res.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                return "Unsupported Content-Type: " + contentType;
            }

          

            // Prepare upload directory safely
            Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            // Generate safe filename
            String extension = allowedTypes.get(contentType);
            String fileName = "upload_" + System.currentTimeMillis() + "." + extension;

            Path filePath = uploadDir.resolve(fileName).normalize();

            // Prevent path traversal (future-proof)
            if (!filePath.startsWith(uploadDir)) {
                res.setStatus(HttpStatus.FORBIDDEN);
                return "Invalid file path";
            }

            // Write file
            Files.write(filePath, bodyBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("File saved: " + filePath);

            return "File uploaded successfully: " + fileName;

        } catch (IOException e) {
            e.printStackTrace();
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            return "Error uploading file";
        }
    }

}
