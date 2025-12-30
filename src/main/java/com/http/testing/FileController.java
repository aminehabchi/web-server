package com.http.testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.http.annotation.classes.Controller;
import com.http.annotation.methodes.Post;
import com.http.config.enums.HttpStatus;
import com.http.http.request.HttpRequest;
import com.http.http.response.HttpResponse;

@Controller
public class FileController {
@Post(path = "/upload")
public String uploadFile(HttpRequest req, HttpResponse res) {
    try {
        // 1️⃣ Get the request body as bytes
        byte[] bodyBytes = req.getBodyAsByte();

        // 2️⃣ Validate body
        if (bodyBytes == null || bodyBytes.length == 0) {
            res.setStatus(HttpStatus.BAD_REQUEST);
            return "Request body is empty!";
        }

        // 3️⃣ Ensure directory exists (optional)
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 4️⃣ Create or overwrite the file
        Path filePath = uploadDir.resolve("uploaded_file.txt");
        if (!Files.exists(filePath)) {
            Files.createFile(filePath); // create new file if not exists
        }

        // 5️⃣ Write the request body into the file
        Files.write(filePath, bodyBytes, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("File saved: " + filePath.toAbsolutePath());

        return "File uploaded and saved successfully!";
    } catch (IOException e) {
        e.printStackTrace();
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return "Error uploading file";
    }
}


}
