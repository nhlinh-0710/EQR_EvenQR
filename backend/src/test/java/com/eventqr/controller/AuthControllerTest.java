package com.eventqr.controller;

import com.eventqr.model.Account;
import com.eventqr.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account("Nguyen Van A", "test@example.com", "password123", "0123456789", "user");
    }

    @Test
    void register_whenSuccess_shouldReturnOkWithAccount() {
        when(authService.register(testAccount)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = authController.register(testAccount);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals("Đăng ký thành công", body.get("message"));
        assertEquals(testAccount, body.get("account"));
        assertEquals("user", body.get("role"));
        verify(authService).register(testAccount);
    }

    @Test
    void register_whenEmailExists_shouldReturnBadRequest() {
        when(authService.register(testAccount)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = authController.register(testAccount);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Email đã tồn tại", body.get("message"));
        verify(authService).register(testAccount);
    }

    @Test
    void login_whenCredentialsValid_shouldReturnOkWithAccount() {
        when(authService.login("test@example.com", "password123")).thenReturn(Optional.of(testAccount));

        ResponseEntity<Map<String, Object>> response = authController.login(testAccount);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals("Đăng nhập thành công", body.get("message"));
        assertEquals(testAccount, body.get("account"));
        assertEquals("user", body.get("role"));
        verify(authService).login("test@example.com", "password123");
    }

    @Test
    void login_whenCredentialsInvalid_shouldReturnOkWithSuccessFalse() {
        when(authService.login("test@example.com", "password123")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, Object>> response = authController.login(testAccount);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Sai email hoặc mật khẩu", body.get("message"));
        verify(authService).login("test@example.com", "password123");
    }

    @Test
    void forgotPassword_whenEmailIsEmpty_shouldReturnBadRequest() {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "");

        ResponseEntity<Map<String, Object>> response = authController.forgotPassword(payload);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Vui lòng nhập email hợp lệ", body.get("message"));
        verifyNoInteractions(authService);
    }

    @Test
    void forgotPassword_whenEmailIsPresent_shouldReturnOk() {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "test@example.com");

        ResponseEntity<Map<String, Object>> response = authController.forgotPassword(payload);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals("Yêu cầu đã được ghi nhận. Vui lòng kiểm tra email.", body.get("message"));
        verifyNoInteractions(authService);
    }
}
