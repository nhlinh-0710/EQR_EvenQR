package com.eventqr.service;

import com.eventqr.model.Notification;
import com.eventqr.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private Notification savedNotification;
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2025, 6, 15, 10, 0);
        savedNotification = createNotification(100L, 1L, 1L,
                "Đăng ký sự kiện mới",
                "User TestUser vừa đăng ký sự kiện TestEvent",
                Notification.NotificationStatus.unread,
                fixedTime);
    }

    private Notification createNotification(Long notificationId, Long userId, Long eventId,
                                             String title, String message,
                                             Notification.NotificationStatus status,
                                             LocalDateTime createdAt) {
        Notification n = new Notification();
        n.setNotificationId(notificationId);
        n.setUserId(userId);
        n.setEventId(eventId);
        n.setTitle(title);
        n.setMessage(message);
        n.setStatus(status);
        n.setCreatedAt(createdAt);
        return n;
    }

    @Test
    void sendToOrganizer_success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendToOrganizer(1L, 1L, "TestEvent", "TestUser");

        verify(notificationRepository).save(any(Notification.class));
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/organizer/1"), dataCaptor.capture());
        Map<String, Object> data = dataCaptor.getValue();
        assertEquals(100L, data.get("notificationId"));
        assertEquals(1L, data.get("userId"));
        assertEquals(1L, data.get("eventId"));
        assertEquals("Đăng ký sự kiện mới", data.get("title"));
        assertEquals("User TestUser vừa đăng ký sự kiện TestEvent", data.get("message"));
        assertEquals("unread", data.get("status"));
        assertEquals(fixedTime.toString(), data.get("createdAt"));
        assertEquals("registration", data.get("type"));
    }

    @Test
    void sendToOrganizer_nullOrganizerId() {
        notificationService.sendToOrganizer(null, 1L, "title", "user");
        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendToOrganizer_nullEventId() {
        notificationService.sendToOrganizer(1L, null, "title", "user");
        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendToOrganizer_nullEventTitle() {
        notificationService.sendToOrganizer(1L, 1L, null, "user");
        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendToOrganizer_nullUserName() {
        notificationService.sendToOrganizer(1L, 1L, "title", null);
        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendToOrganizer_saveThrowsException() {
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("DB error"));

        notificationService.sendToOrganizer(1L, 1L, "title", "user");

        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendToOrganizer_convertAndSendThrowsException() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        doThrow(new RuntimeException("WS error")).when(messagingTemplate).convertAndSend(any(), any());

        notificationService.sendToOrganizer(1L, 1L, "title", "user");

        verify(notificationRepository).save(any());
    }

    @Test
    void sendToOrganizer_createdAtNull() {
        savedNotification.setCreatedAt(null);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        LocalDateTime before = LocalDateTime.now();
        notificationService.sendToOrganizer(1L, 1L, "TestEvent", "TestUser");
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(anyString(), dataCaptor.capture());
        String createdAtStr = (String) dataCaptor.getValue().get("createdAt");
        assertNotNull(createdAtStr);
        LocalDateTime parsed = LocalDateTime.parse(createdAtStr);
        assertTrue(!parsed.isBefore(before) || parsed.isEqual(before));
        assertTrue(!parsed.isAfter(after) || parsed.isEqual(after));
    }

    @Test
    void getNotificationsByUserId_success() {
        List<Notification> notifications = List.of(savedNotification);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(notifications);

        List<Notification> result = notificationService.getNotificationsByUserId(1L);

        assertEquals(1, result.size());
        assertEquals(savedNotification.getNotificationId(), result.get(0).getNotificationId());
    }

    @Test
    void getUnreadNotifications_success() {
        List<Notification> unreadList = List.of(savedNotification);
        when(notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(eq(1L),
                eq(Notification.NotificationStatus.unread))).thenReturn(unreadList);

        List<Notification> result = notificationService.getUnreadNotifications(1L);

        assertEquals(1, result.size());
        assertEquals(Notification.NotificationStatus.unread, result.get(0).getStatus());
    }

    @Test
    void markAsRead_success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(savedNotification));

        notificationService.markAsRead(100L, 1L);

        assertEquals(Notification.NotificationStatus.read, savedNotification.getStatus());
        verify(notificationRepository).save(savedNotification);
    }

    @Test
    void markAsRead_notificationNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(999L, 1L));
    }

    @Test
    void markAsRead_userIdMismatch() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(savedNotification));

        assertThrows(IllegalStateException.class, () -> notificationService.markAsRead(100L, 99L));
    }

    @Test
    void countUnreadNotifications_success() {
        when(notificationRepository.countByUserIdAndStatus(eq(1L),
                eq(Notification.NotificationStatus.unread))).thenReturn(5L);

        long count = notificationService.countUnreadNotifications(1L);

        assertEquals(5L, count);
    }

    @Test
    void sendFeedbackNotificationToOrganizer_success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendFeedbackNotificationToOrganizer(1L, 1L, "TestEvent", "TestUser", 5);

        verify(notificationRepository).save(any(Notification.class));
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/organizer/1"), dataCaptor.capture());
        assertEquals("feedback", dataCaptor.getValue().get("type"));
    }

    @Test
    void sendFeedbackNotificationToOrganizer_nullRating() {
        savedNotification.setTitle("Có đánh giá mới cho sự kiện");
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendFeedbackNotificationToOrganizer(1L, 1L, "TestEvent", "TestUser", null);

        verify(notificationRepository).save(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getMessage().contains("0 sao"));
        verify(messagingTemplate).convertAndSend(anyString(), any(Map.class));
    }

    @Test
    void sendFeedbackNotificationToOrganizer_nullOrganizerId() {
        notificationService.sendFeedbackNotificationToOrganizer(null, 1L, "title", "user", 5);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendFeedbackNotificationToOrganizer_nullEventId() {
        notificationService.sendFeedbackNotificationToOrganizer(1L, null, "title", "user", 5);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendFeedbackNotificationToOrganizer_nullEventTitle() {
        notificationService.sendFeedbackNotificationToOrganizer(1L, 1L, null, "user", 5);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendFeedbackNotificationToOrganizer_nullUserName() {
        notificationService.sendFeedbackNotificationToOrganizer(1L, 1L, "title", null, 5);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendFeedbackNotificationToOrganizer_saveThrowsException() {
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("DB error"));

        notificationService.sendFeedbackNotificationToOrganizer(1L, 1L, "title", "user", 5);

        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendFeedbackNotificationToOrganizer_convertAndSendThrowsException() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        doThrow(new RuntimeException("WS error")).when(messagingTemplate).convertAndSend(any(), any());

        notificationService.sendFeedbackNotificationToOrganizer(1L, 1L, "title", "user", 5);

        verify(notificationRepository).save(any());
    }

    @Test
    void sendFeedbackReplyNotificationToUser_success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendFeedbackReplyNotificationToUser(1L, 1L, "TestEvent", "Cảm ơn bạn!");

        verify(notificationRepository).save(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getMessage().contains("Cảm ơn bạn!"));
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user/1"), dataCaptor.capture());
        assertEquals("feedback_reply", dataCaptor.getValue().get("type"));
    }

    @Test
    void sendFeedbackReplyNotificationToUser_replyMoreThan100Chars() {
        String longReply = "a".repeat(150);
        String expectedTruncated = "a".repeat(100) + "...";
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendFeedbackReplyNotificationToUser(1L, 1L, "TestEvent", longReply);

        verify(notificationRepository).save(notificationCaptor.capture());
        String message = notificationCaptor.getValue().getMessage();
        assertTrue(message.contains(expectedTruncated));
        assertFalse(message.contains(longReply));
        verify(messagingTemplate).convertAndSend(anyString(), any(Map.class));
    }

    @Test
    void sendFeedbackReplyNotificationToUser_nullUserId() {
        notificationService.sendFeedbackReplyNotificationToUser(null, 1L, "title", "reply");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendFeedbackReplyNotificationToUser_nullEventId() {
        notificationService.sendFeedbackReplyNotificationToUser(1L, null, "title", "reply");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendFeedbackReplyNotificationToUser_nullEventTitle() {
        notificationService.sendFeedbackReplyNotificationToUser(1L, 1L, null, "reply");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendFeedbackReplyNotificationToUser_nullOrganizerReply() {
        notificationService.sendFeedbackReplyNotificationToUser(1L, 1L, "title", null);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendFeedbackReplyNotificationToUser_saveThrowsException() {
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("DB error"));

        notificationService.sendFeedbackReplyNotificationToUser(1L, 1L, "title", "reply");

        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendFeedbackReplyNotificationToUser_convertAndSendThrowsException() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        doThrow(new RuntimeException("WS error")).when(messagingTemplate).convertAndSend(any(), any());

        notificationService.sendFeedbackReplyNotificationToUser(1L, 1L, "title", "reply");

        verify(notificationRepository).save(any());
    }

    @Test
    void sendEventStartSoonNotification_success() {
        savedNotification.setTitle("Sự kiện sắp bắt đầu");
        savedNotification.setMessage("Sự kiện \"TestEvent\" sẽ bắt đầu vào 15/06/2025 11:00. Hãy chuẩn bị tham gia!");
        when(notificationRepository.existsByUserIdAndEventIdAndTitle(anyLong(), anyLong(), anyString())).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendEventStartSoonNotification(1L, 1L, "TestEvent",
                LocalDateTime.of(2025, 6, 15, 11, 0));

        verify(notificationRepository).save(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getMessage().contains("15/06/2025 11:00"));
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user/1"), dataCaptor.capture());
        assertEquals("event_start_soon", dataCaptor.getValue().get("type"));
    }

    @Test
    void sendEventStartSoonNotification_nullStartTime() {
        savedNotification.setTitle("Sự kiện sắp bắt đầu");
        when(notificationRepository.existsByUserIdAndEventIdAndTitle(anyLong(), anyLong(), anyString())).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendEventStartSoonNotification(1L, 1L, "TestEvent", null);

        verify(notificationRepository).save(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getMessage().contains("sắp tới"));
        verify(messagingTemplate).convertAndSend(anyString(), any(Map.class));
    }

    @Test
    void sendEventStartSoonNotification_duplicateExists() {
        when(notificationRepository.existsByUserIdAndEventIdAndTitle(anyLong(), anyLong(), anyString())).thenReturn(true);

        notificationService.sendEventStartSoonNotification(1L, 1L, "TestEvent", LocalDateTime.now());

        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendEventStartSoonNotification_nullUserId() {
        notificationService.sendEventStartSoonNotification(null, 1L, "title", LocalDateTime.now());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendEventStartSoonNotification_nullEventId() {
        notificationService.sendEventStartSoonNotification(1L, null, "title", LocalDateTime.now());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendEventStartSoonNotification_nullEventTitle() {
        notificationService.sendEventStartSoonNotification(1L, 1L, null, LocalDateTime.now());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendEventEndedNotification_success() {
        savedNotification.setTitle("Sự kiện đã kết thúc");
        savedNotification.setMessage("Sự kiện \"TestEvent\" đã kết thúc. Hãy chia sẻ đánh giá của bạn để giúp chúng tôi cải thiện!");
        when(notificationRepository.existsByUserIdAndEventIdAndTitle(anyLong(), anyLong(), anyString())).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.sendEventEndedNotification(1L, 1L, "TestEvent");

        verify(notificationRepository).save(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getMessage().contains("đã kết thúc"));
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user/1"), dataCaptor.capture());
        assertEquals("event_ended", dataCaptor.getValue().get("type"));
    }

    @Test
    void sendEventEndedNotification_duplicateExists() {
        when(notificationRepository.existsByUserIdAndEventIdAndTitle(anyLong(), anyLong(), anyString())).thenReturn(true);

        notificationService.sendEventEndedNotification(1L, 1L, "TestEvent");

        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(any(), any());
    }

    @Test
    void sendEventEndedNotification_nullUserId() {
        notificationService.sendEventEndedNotification(null, 1L, "title");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendEventEndedNotification_nullEventId() {
        notificationService.sendEventEndedNotification(1L, null, "title");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void sendEventEndedNotification_nullEventTitle() {
        notificationService.sendEventEndedNotification(1L, 1L, null);
        verify(notificationRepository, never()).save(any());
    }
}
