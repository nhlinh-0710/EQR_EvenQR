package com.eventqr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir:uploads/images/events}")
    private String uploadDir;
    
    private Path uploadPath;

    /**
     * Khởi tạo và tạo thư mục upload nếu chưa tồn tại
     */
    @PostConstruct
    public void init() {
        try {
            // Chuyển đổi đường dẫn thành Path object
            // Hỗ trợ cả đường dẫn tuyệt đối (C:/folder) và tương đối (uploads/images)
            this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            
            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(this.uploadPath)) {
                Files.createDirectories(this.uploadPath);
                logger.info("✅ Đã tạo thư mục upload: {}", this.uploadPath);
            } else {
                logger.info("✅ Thư mục upload đã sẵn sàng: {}", this.uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ file: " + uploadDir, e);
        }
    }

    /**
     * Lưu file ảnh vào thư mục và trả về đường dẫn đầy đủ
     */
    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (jpg, png, gif, webp)");
        }

        // Validate file size (đã được giới hạn trong application.properties nhưng check thêm)
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("Kích thước file không được vượt quá 10MB");
        }

        // Tạo tên file unique để tránh trùng lặp
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Lưu file vào thư mục
        Path targetLocation = this.uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn đầy đủ của file để lưu vào database
        String savedPath = targetLocation.toString();
        logger.info("✅ Đã lưu file: {}", savedPath);
        return savedPath;
    }

    /**
     * Xóa file ảnh cũ (nếu có)
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            // Chuyển đổi thành Path object
            Path fileToDelete = Paths.get(filePath);
            
            // Xóa file nếu tồn tại
            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
                logger.info("✅ Đã xóa file: {}", filePath);
            } else {
                logger.warn("⚠️ File không tồn tại: {}", filePath);
            }
        } catch (IOException e) {
            // Log error nhưng không throw exception vì xóa file không quan trọng bằng lưu file mới
            logger.error("❌ Không thể xóa file: {} - {}", filePath, e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra file có phải là ảnh hợp lệ không
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        // Chấp nhận các định dạng ảnh phổ biến
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    /**
     * Lấy đường dẫn thư mục upload hiện tại
     */
    public String getUploadPath() {
        return this.uploadPath != null ? this.uploadPath.toString() : uploadDir;
    }
}

