package com.eventqr.controller;

import com.eventqr.model.Account;
import com.eventqr.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Account account) {
        Map<String, Object> response = new HashMap<>();
        boolean success = authService.register(account);
        if (success) {
            response.put("success", true);
            response.put("message", "Đăng ký thành công");
            response.put("account", account);
            response.put("role", account.getRole());
            return ResponseEntity.ok(response);
        }
        response.put("success", false);
        response.put("message", "Email đã tồn tại");
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Account account) {
        Map<String, Object> response = new HashMap<>();
        Optional<Account> found = authService.login(account.getEmail(), account.getPassword());
        if (found.isPresent()) {
            Account userAccount = found.get();
            response.put("success", true);
            response.put("message", "Đăng nhập thành công");
            response.put("account", userAccount);
            response.put("role", userAccount.getRole());
            return ResponseEntity.ok(response);
        }
        response.put("success", false);
        response.put("message", "Sai email hoặc mật khẩu");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String email = payload.getOrDefault("email", "").trim();
        if (email.isEmpty()) {
            response.put("success", false);
            response.put("message", "Vui lòng nhập email hợp lệ");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("success", true);
        response.put("message", "Yêu cầu đã được ghi nhận. Vui lòng kiểm tra email.");
        return ResponseEntity.ok(response);
    }
}
