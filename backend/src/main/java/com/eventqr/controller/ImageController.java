package com.eventqr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg");
    private static final Path BASE_DIR = Paths.get("uploads").toAbsolutePath().normalize();

    @GetMapping("/view")
    public ResponseEntity<Resource> getImage(@RequestParam String path) {
        try {
            Path filePath = Paths.get(path).normalize();

            if (!filePath.startsWith(BASE_DIR)) {
                logger.warn("Truy cập trái phép: {}", path);
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                logger.warn("File không tồn tại: {}", path);
                return ResponseEntity.notFound().build();
            }

            String filename = filePath.getFileName().toString().toLowerCase();
            boolean hasValidExtension = ALLOWED_EXTENSIONS.stream().anyMatch(filename::endsWith);
            if (!hasValidExtension) {
                logger.warn("Định dạng file không được phép: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Lỗi khi đọc file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    private String determineContentType(Path filePath) {
        String filename = filePath.getFileName().toString().toLowerCase();
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".gif")) return "image/gif";
        if (filename.endsWith(".webp")) return "image/webp";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }
}
