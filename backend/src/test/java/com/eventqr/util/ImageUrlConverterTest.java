package com.eventqr.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageUrlConverterTest {

    @Test
    void constructorCanBeInstantiated() {
        assertDoesNotThrow(ImageUrlConverter::new);
    }

    @Test
    void convertToAccessibleUrl_nullInput_returnsNull() {
        assertNull(ImageUrlConverter.convertToAccessibleUrl(null));
    }

    @Test
    void convertToAccessibleUrl_emptyInput_returnsNull() {
        assertNull(ImageUrlConverter.convertToAccessibleUrl(""));
    }

    @Test
    void convertToAccessibleUrl_httpUrl_returnsSame() {
        String url = "http://example.com/image.jpg";
        assertEquals(url, ImageUrlConverter.convertToAccessibleUrl(url));
    }

    @Test
    void convertToAccessibleUrl_httpsUrl_returnsSame() {
        String url = "https://example.com/image.jpg";
        assertEquals(url, ImageUrlConverter.convertToAccessibleUrl(url));
    }

    @Test
    void convertToAccessibleUrl_absolutePathStartingWithSlash_returnsSame() {
        String path = "/uploads/images/photo.jpg";
        assertEquals(path, ImageUrlConverter.convertToAccessibleUrl(path));
    }

    @Test
    void convertToAccessibleUrl_regularPath_returnsEncodedUrl() {
        String result = ImageUrlConverter.convertToAccessibleUrl("D:/uploads/photo.jpg");
        assertEquals("/api/images/view?path=D%3A%2Fuploads%2Fphoto.jpg", result);
    }

    @Test
    void convertToAccessibleUrl_pathWithSpaces_encodesSpace() {
        String result = ImageUrlConverter.convertToAccessibleUrl("D:/uploads/my path/file.jpg");
        assertTrue(result.contains("my+path"));
        assertTrue(result.startsWith("/api/images/view?path="));
    }

    @Test
    void convertToAccessibleUrl_pathWithSpecialCharacters_encodesThem() {
        String result = ImageUrlConverter.convertToAccessibleUrl("D:/uploads/photo & file.jpg");
        assertTrue(result.startsWith("/api/images/view?path="));
        assertFalse(result.contains(" "));
        assertFalse(result.contains("&"));
    }
}
