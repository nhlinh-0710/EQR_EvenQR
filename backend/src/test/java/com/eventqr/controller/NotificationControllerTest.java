package com.eventqr.controller;

import com.eventqr.model.Notification;
import com.eventqr.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController controller;

    @Test
    void getNotificationsByUserId_shouldReturnNotifications() {
        List<Notification> notifications = List.of(new Notification(), new Notification());
        when(notificationService.getNotificationsByUserId(1L)).thenReturn(notifications);

        var response = controller.getNotificationsByUserId(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(notifications, response.getBody());
    }

    @Test
    void getNotificationsByUserId_whenException_shouldReturnBadRequest() {
        when(notificationService.getNotificationsByUserId(1L)).thenThrow(new RuntimeException("error"));

        var response = controller.getNotificationsByUserId(1L);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void getUnreadNotifications_shouldReturnUnread() {
        List<Notification> unread = List.of(new Notification());
        when(notificationService.getUnreadNotifications(1L)).thenReturn(unread);

        var response = controller.getUnreadNotifications(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(unread, response.getBody());
    }

    @Test
    void getUnreadNotifications_whenException_shouldReturnBadRequest() {
        when(notificationService.getUnreadNotifications(1L)).thenThrow(new RuntimeException("error"));

        var response = controller.getUnreadNotifications(1L);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void countUnreadNotifications_shouldReturnCount() {
        when(notificationService.countUnreadNotifications(1L)).thenReturn(5L);

        var response = controller.countUnreadNotifications(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(5L, body.get("count"));
    }

    @Test
    void countUnreadNotifications_whenException_shouldReturnBadRequest() {
        when(notificationService.countUnreadNotifications(1L)).thenThrow(new RuntimeException("error"));

        var response = controller.countUnreadNotifications(1L);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void markAsRead_shouldReturnOk() {
        doNothing().when(notificationService).markAsRead(1L, 1L);

        var response = controller.markAsRead(1L, 1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
    }

    @Test
    void markAsRead_whenIllegalState_shouldReturn403() {
        doThrow(new IllegalStateException("no permission")).when(notificationService).markAsRead(1L, 1L);

        var response = controller.markAsRead(1L, 1L);

        assertEquals(403, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void markAsRead_whenException_shouldReturnBadRequest() {
        doThrow(new RuntimeException("error")).when(notificationService).markAsRead(1L, 1L);

        var response = controller.markAsRead(1L, 1L);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }
}
