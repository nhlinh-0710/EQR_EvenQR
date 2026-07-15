package com.eventqr.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class ImageControllerTest {

    private Path baseDir;
    private Path tempDir;
    private ImageController controller;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("img-test");
        baseDir = tempDir.resolve("uploads");
        Files.createDirectories(baseDir);

        setStaticField(ImageController.class, "BASE_DIR", baseDir);

        controller = new ImageController();
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(tempDir)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    private void setStaticField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, value);
    }

    @Test
    void getImage_withValidPath_shouldReturnResource() throws Exception {
        Path file = baseDir.resolve("test.jpg");
        Files.createFile(file);

        ResponseEntity<Resource> response = controller.getImage(file.toString());

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().getContentType().toString().contains("image/jpeg"));
    }

    @Test
    void getImage_withPathTraversal_shouldReturnBadRequest() {
        ResponseEntity<Resource> response = controller.getImage(tempDir.resolve("outside.txt").toString());

        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void getImage_whenFileNotFound_shouldReturnNotFound() throws Exception {
        Path file = baseDir.resolve("nonexistent.jpg");

        ResponseEntity<Resource> response = controller.getImage(file.toString());

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getImage_withInvalidExtension_shouldReturnBadRequest() throws Exception {
        Path file = baseDir.resolve("test.txt");
        Files.createFile(file);

        ResponseEntity<Resource> response = controller.getImage(file.toString());

        assertTrue(response.getStatusCode().is4xxClientError());
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void getImage_whenException_shouldReturnBadRequest() {
        ResponseEntity<Resource> response = controller.getImage((String) null);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void determineContentType_forJpg_shouldReturnImageJpeg() throws Exception {
        Method method = ImageController.class.getDeclaredMethod("determineContentType", Path.class);
        method.setAccessible(true);

        assertEquals("image/jpeg", method.invoke(controller, Paths.get("test.jpg")));
        assertEquals("image/jpeg", method.invoke(controller, Paths.get("test.jpeg")));
    }

    @Test
    void determineContentType_forPng_shouldReturnImagePng() throws Exception {
        Method method = ImageController.class.getDeclaredMethod("determineContentType", Path.class);
        method.setAccessible(true);

        assertEquals("image/png", method.invoke(controller, Paths.get("test.png")));
    }

    @Test
    void determineContentType_forGif_shouldReturnImageGif() throws Exception {
        Method method = ImageController.class.getDeclaredMethod("determineContentType", Path.class);
        method.setAccessible(true);

        assertEquals("image/gif", method.invoke(controller, Paths.get("test.gif")));
    }

    @Test
    void determineContentType_forWebp_shouldReturnImageWebp() throws Exception {
        Method method = ImageController.class.getDeclaredMethod("determineContentType", Path.class);
        method.setAccessible(true);

        assertEquals("image/webp", method.invoke(controller, Paths.get("test.webp")));
    }

    @Test
    void determineContentType_forSvg_shouldReturnImageSvgXml() throws Exception {
        Method method = ImageController.class.getDeclaredMethod("determineContentType", Path.class);
        method.setAccessible(true);

        assertEquals("image/svg+xml", method.invoke(controller, Paths.get("test.svg")));
    }

    @Test
    void determineContentType_forUnknown_shouldReturnOctetStream() throws Exception {
        Method method = ImageController.class.getDeclaredMethod("determineContentType", Path.class);
        method.setAccessible(true);

        assertEquals("application/octet-stream", method.invoke(controller, Paths.get("test.bin")));
        assertEquals("application/octet-stream", method.invoke(controller, Paths.get("test")));
    }
}
