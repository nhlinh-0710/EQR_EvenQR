package com.eventqr.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Utility class để convert đường dẫn file thành URL có thể truy cập được qua API
 */
public class ImageUrlConverter {

    private static final Logger logger = LoggerFactory.getLogger(ImageUrlConverter.class);

    /**
     * Convert đường dẫn file thành URL có thể truy cập được qua API
     * 
     * @param imageUrl Đường dẫn file (có thể là tuyệt đối, tương đối, hoặc URL)
     * @return URL có thể truy cập được từ frontend
     */
    public static String convertToAccessibleUrl(String imageUrl) {
        // Nếu không có ảnh, trả về null
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        // Nếu đã là URL (http/https), giữ nguyên
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl;
        }
        
        // Nếu là đường dẫn tương đối bắt đầu bằng /, giữ nguyên (frontend sẽ xử lý)
        if (imageUrl.startsWith("/")) {
            return imageUrl;
        }
        
        // Nếu là đường dẫn tuyệt đối (như D:/... hoặc /home/...), convert thành API URL
        try {
            String encodedPath = URLEncoder.encode(imageUrl, "UTF-8");
            return "/api/images/view?path=" + encodedPath;
        } catch (UnsupportedEncodingException e) {
            logger.error("❌ Lỗi khi encode image URL: {}", e.getMessage(), e);
            return null;
        }
    }
}

