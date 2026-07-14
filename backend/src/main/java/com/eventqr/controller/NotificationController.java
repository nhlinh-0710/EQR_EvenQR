package com.eventqr.controller;

import com.eventqr.model.Notification;
import com.eventqr.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications/user/{userId}
     * Lấy tất cả thông báo của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getNotificationsByUserId(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * GET /api/notifications/user/{userId}/unread
     * Lấy thông báo chưa đọc
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable Long userId) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * GET /api/notifications/user/{userId}/count
     * Đếm số thông báo chưa đọc
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<?> countUnreadNotifications(@PathVariable Long userId) {
        try {
            long count = notificationService.countUnreadNotifications(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    /**
     * PUT /api/notifications/{notificationId}/read
     * Đánh dấu thông báo là đã đọc
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try {
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã đánh dấu đã đọc"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(
                Map.of("success", false, "message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        }
    }
}

