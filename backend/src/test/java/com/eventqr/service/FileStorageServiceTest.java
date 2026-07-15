package com.eventqr.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() throws Exception {
        fileStorageService = new FileStorageService();
        Field uploadPathField = FileStorageService.class.getDeclaredField("uploadPath");
        uploadPathField.setAccessible(true);
        uploadPathField.set(fileStorageService, tempDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        Field uploadPathField = FileStorageService.class.getDeclaredField("uploadPath");
        uploadPathField.setAccessible(true);
        uploadPathField.set(fileStorageService, null);
    }

    @Test
    void init_whenDirectoryDoesNotExist_shouldCreateIt() throws Exception {
        Path newDir = tempDir.resolve("new_upload_dir");
        FileStorageService service = new FileStorageService();
        Field uploadPathField = FileStorageService.class.getDeclaredField("uploadPath");
        uploadPathField.setAccessible(true);

        service.init();
        assertFalse(Files.exists(newDir));

        uploadPathField.set(service, newDir);
        service.init();
        assertTrue(Files.exists(newDir));
    }

    @Test
    void init_whenDirectoryExists_shouldReuse() throws Exception {
        assertTrue(Files.exists(tempDir));
        fileStorageService.init();
        assertTrue(Files.exists(tempDir));
    }

    @Test
    void saveFile_whenFileIsNull_shouldReturnNull() throws IOException {
        assertNull(fileStorageService.saveFile(null));
    }

    @Test
    void saveFile_whenFileIsEmpty_shouldReturnNull() throws IOException {
        MultipartFile emptyFile = createMultipartFile("empty.jpg", "image/jpeg", new byte[0]);
        assertNull(fileStorageService.saveFile(emptyFile));
    }

    @Test
    void saveFile_whenNonImageContentType_shouldThrow() {
        MultipartFile nonImage = createMultipartFile("doc.pdf", "application/pdf", new byte[]{1, 2, 3});
        assertThrows(IllegalArgumentException.class, () -> fileStorageService.saveFile(nonImage));
    }

    @Test
    void saveFile_whenFileExceeds10MB_shouldThrow() {
        byte[] largeContent = new byte[11 * 1024 * 1024];
        MultipartFile largeFile = createMultipartFile("large.jpg", "image/jpeg", largeContent);
        assertThrows(IllegalArgumentException.class, () -> fileStorageService.saveFile(largeFile));
    }

    @Test
    void saveFile_whenValidImage_shouldSaveAndReturnPath() throws IOException {
        byte[] content = new byte[]{1, 2, 3, 4, 5};
        MultipartFile validFile = createMultipartFile("photo.jpg", "image/jpeg", content);

        String savedPath = fileStorageService.saveFile(validFile);

        assertNotNull(savedPath);
        assertTrue(Files.exists(Path.of(savedPath)));
        assertArrayEquals(content, Files.readAllBytes(Path.of(savedPath)));
    }

    @Test
    void deleteFile_whenPathIsNull_shouldDoNothing() {
        fileStorageService.deleteFile(null);
    }

    @Test
    void deleteFile_whenPathIsEmpty_shouldDoNothing() {
        fileStorageService.deleteFile("");
    }

    @Test
    void deleteFile_whenFileExists_shouldDelete() throws IOException {
        Path fileToDelete = tempDir.resolve("delete_me.txt");
        Files.writeString(fileToDelete, "content");
        assertTrue(Files.exists(fileToDelete));

        fileStorageService.deleteFile(fileToDelete.toString());
        assertFalse(Files.exists(fileToDelete));
    }

    @Test
    void deleteFile_whenFileDoesNotExist_shouldNotThrow() {
        Path nonExistent = tempDir.resolve("ghost.txt");
        assertFalse(Files.exists(nonExistent));
        fileStorageService.deleteFile(nonExistent.toString());
    }

    @Test
    void isValidImageFile_whenFileIsNull_shouldReturnFalse() {
        assertFalse(fileStorageService.isValidImageFile(null));
    }

    @Test
    void isValidImageFile_whenFileIsEmpty_shouldReturnFalse() {
        MultipartFile emptyFile = createMultipartFile("empty.jpg", "image/jpeg", new byte[0]);
        assertFalse(fileStorageService.isValidImageFile(emptyFile));
    }

    @Test
    void isValidImageFile_whenContentTypeIsNull_shouldReturnFalse() {
        MultipartFile noType = createMultipartFile("img.jpg", null, new byte[]{1});
        assertFalse(fileStorageService.isValidImageFile(noType));
    }

    @Test
    void isValidImageFile_whenValidTypes_shouldReturnTrue() {
        assertTrue(fileStorageService.isValidImageFile(createMultipartFile("a.jpeg", "image/jpeg", new byte[]{1})));
        assertTrue(fileStorageService.isValidImageFile(createMultipartFile("a.jpg", "image/jpg", new byte[]{1})));
        assertTrue(fileStorageService.isValidImageFile(createMultipartFile("a.png", "image/png", new byte[]{1})));
        assertTrue(fileStorageService.isValidImageFile(createMultipartFile("a.gif", "image/gif", new byte[]{1})));
        assertTrue(fileStorageService.isValidImageFile(createMultipartFile("a.webp", "image/webp", new byte[]{1})));
    }

    @Test
    void isValidImageFile_whenInvalidTypes_shouldReturnFalse() {
        assertFalse(fileStorageService.isValidImageFile(createMultipartFile("a.pdf", "application/pdf", new byte[]{1})));
        assertFalse(fileStorageService.isValidImageFile(createMultipartFile("a.txt", "text/plain", new byte[]{1})));
        assertFalse(fileStorageService.isValidImageFile(createMultipartFile("a.svg", "image/svg+xml", new byte[]{1})));
    }

    @Test
    void getUploadPath_shouldReturnPathString() {
        String path = fileStorageService.getUploadPath();
        assertEquals(tempDir.toString(), path);
    }

    private MultipartFile createMultipartFile(String name, String contentType, byte[] content) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getOriginalFilename() {
                return name;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return content == null || content.length == 0;
            }

            @Override
            public long getSize() {
                return content != null ? content.length : 0;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return content;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(content != null ? content : new byte[0]);
            }

            @Override
            public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), content != null ? content : new byte[0]);
            }
        };
    }
}
