package com.eventqr.service;

import com.eventqr.model.Notification;
import com.eventqr.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, 
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Gửi thông báo realtime tới organizer khi có user đăng ký sự kiện
     * 
     * @param organizerId ID của organizer (người nhận thông báo)
     * @param eventId ID của sự kiện
     * @param eventTitle Tên sự kiện
     * @param userName Tên user đăng ký
     */
    // LINH
    @Transactional
    public void sendToOrganizer(Long organizerId, Long eventId, String eventTitle, String userName) {
        try {
            // Validate input
            if (organizerId == null || eventId == null || eventTitle == null || userName == null) {
                throw new IllegalArgumentException("Thông tin không đầy đủ để gửi thông báo");
            }

            // Tạo nội dung thông báo
            String message = String.format("User %s vừa đăng ký sự kiện %s", userName, eventTitle);
            String title = "Đăng ký sự kiện mới";

            // Lưu vào database
            Notification notification = new Notification();
            notification.setUserId(organizerId);
            notification.setEventId(eventId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setStatus(Notification.NotificationStatus.unread);
            
            Notification savedNotification = notificationRepository.save(notification);

            // Tạo JSON response để gửi qua WebSocket
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notificationId", savedNotification.getNotificationId());
            notificationData.put("userId", savedNotification.getUserId());
            notificationData.put("eventId", savedNotification.getEventId());
            notificationData.put("title", savedNotification.getTitle());
            notificationData.put("message", savedNotification.getMessage());
            notificationData.put("status", savedNotification.getStatus().name());
            notificationData.put("createdAt", savedNotification.getCreatedAt() != null 
                ? savedNotification.getCreatedAt().toString() 
                : java.time.LocalDateTime.now().toString());
            notificationData.put("type", "registration"); // Loại thông báo

            // Gửi thông báo realtime qua WebSocket tới topic: /topic/organizer/{organizerId}
            String destination = "/topic/organizer/" + organizerId;
            messagingTemplate.convertAndSend(destination, notificationData);
            
            logger.info("✅ Đã gửi thông báo WebSocket tới: {}", destination);
        } catch (Exception e) {
            // Log lỗi nhưng không throw để không ảnh hưởng đến quá trình đăng ký
            logger.error("❌ Lỗi khi gửi thông báo WebSocket: {}", e.getMessage(), e);
        }
    }
    // LINH

    /**
     * Lấy danh sách thông báo của user
     */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Lấy danh sách thông báo chưa đọc
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
            userId, 
            Notification.NotificationStatus.unread
        );
    }

    /**
     * Đánh dấu thông báo là đã đọc
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Thông báo không tồn tại"));
        
        // Kiểm tra quyền: chỉ user sở hữu mới được đánh dấu đã đọc
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền truy cập thông báo này");
        }
        
        notification.setStatus(Notification.NotificationStatus.read);
        notificationRepository.save(notification);
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndStatus(
            userId, 
            Notification.NotificationStatus.unread
        );
    }

    /**
     * Gửi thông báo realtime tới organizer khi có user đánh giá sự kiện
     * 
     * @param organizerId ID của organizer (người nhận thông báo)
     * @param eventId ID của sự kiện
     * @param eventTitle Tên sự kiện
     * @param userName Tên user đánh giá
     * @param rating Điểm đánh giá
     */
    @Transactional
    public void sendFeedbackNotificationToOrganizer(Long organizerId, Long eventId, 
                                                     String eventTitle, String userName, Integer rating) {
        try {
            // Validate input
            if (organizerId == null || eventId == null || eventTitle == null || userName == null) {
                throw new IllegalArgumentException("Thông tin không đầy đủ để gửi thông báo feedback");
            }

            // Tạo nội dung thông báo
            String message = String.format("User %s vừa đánh giá sự kiện %s với %d sao", 
                userName, eventTitle, rating != null ? rating : 0);
            String title = "Có đánh giá mới cho sự kiện";

            // Lưu vào database
            Notification notification = new Notification();
            notification.setUserId(organizerId);
            notification.setEventId(eventId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setStatus(Notification.NotificationStatus.unread);
            
            Notification savedNotification = notificationRepository.save(notification);

            // Tạo JSON response để gửi qua WebSocket
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notificationId", savedNotification.getNotificationId());
            notificationData.put("userId", savedNotification.getUserId());
            notificationData.put("eventId", savedNotification.getEventId());
            notificationData.put("title", savedNotification.getTitle());
            notificationData.put("message", savedNotification.getMessage());
            notificationData.put("status", savedNotification.getStatus().name());
            notificationData.put("createdAt", savedNotification.getCreatedAt() != null 
                ? savedNotification.getCreatedAt().toString() 
                : java.time.LocalDateTime.now().toString());
            notificationData.put("type", "feedback"); // Loại thông báo

            // Gửi thông báo realtime qua WebSocket tới topic: /topic/organizer/{organizerId}
            String destination = "/topic/organizer/" + organizerId;
            messagingTemplate.convertAndSend(destination, notificationData);
            
            logger.info("✅ Đã gửi thông báo feedback WebSocket tới: {}", destination);
        } catch (Exception e) {
            // Log lỗi nhưng không throw để không ảnh hưởng đến quá trình feedback
            logger.error("❌ Lỗi khi gửi thông báo feedback WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi thông báo realtime tới user khi organizer trả lời feedback
     * 
     * @param userId ID của user (người nhận thông báo)
     * @param eventId ID của sự kiện
     * @param eventTitle Tên sự kiện
     * @param organizerReply Nội dung phản hồi của organizer
     */
    @Transactional
    public void sendFeedbackReplyNotificationToUser(Long userId, Long eventId, 
                                                     String eventTitle, String organizerReply) {
        try {
            // Validate input
            if (userId == null || eventId == null || eventTitle == null || organizerReply == null) {
                throw new IllegalArgumentException("Thông tin không đầy đủ để gửi thông báo phản hồi feedback");
            }

            // Tạo nội dung thông báo
            String message = String.format("Tổ chức viên đã phản hồi đánh giá của bạn về sự kiện \"%s\": \"%s\"", 
                eventTitle, 
                organizerReply.length() > 100 ? organizerReply.substring(0, 100) + "..." : organizerReply);
            String title = "Có phản hồi về đánh giá của bạn";

            // Lưu vào database
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setEventId(eventId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setStatus(Notification.NotificationStatus.unread);
            
            Notification savedNotification = notificationRepository.save(notification);

            // Tạo JSON response để gửi qua WebSocket
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notificationId", savedNotification.getNotificationId());
            notificationData.put("userId", savedNotification.getUserId());
            notificationData.put("eventId", savedNotification.getEventId());
            notificationData.put("title", savedNotification.getTitle());
            notificationData.put("message", savedNotification.getMessage());
            notificationData.put("status", savedNotification.getStatus().name());
            notificationData.put("createdAt", savedNotification.getCreatedAt() != null 
                ? savedNotification.getCreatedAt().toString() 
                : java.time.LocalDateTime.now().toString());
            notificationData.put("type", "feedback_reply"); // Loại thông báo

            // Gửi thông báo realtime qua WebSocket tới topic: /topic/user/{userId}
            String destination = "/topic/user/" + userId;
            messagingTemplate.convertAndSend(destination, notificationData);
            
            logger.info("✅ Đã gửi thông báo feedback reply WebSocket tới: {}", destination);
        } catch (Exception e) {
            // Log lỗi nhưng không throw để không ảnh hưởng đến quá trình reply
            logger.error("❌ Lỗi khi gửi thông báo feedback reply WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi thông báo cho user khi sự kiện sắp bắt đầu (1 giờ trước)
     * 
     * @param userId ID của user (người nhận thông báo)
     * @param eventId ID của sự kiện
     * @param eventTitle Tên sự kiện
     * @param startTime Thời gian bắt đầu sự kiện
     */
    @Transactional
    public void sendEventStartSoonNotification(Long userId, Long eventId, String eventTitle, java.time.LocalDateTime startTime) {
        try {
            // Validate input
            if (userId == null || eventId == null || eventTitle == null) {
                throw new IllegalArgumentException("Thông tin không đầy đủ để gửi thông báo sự kiện sắp bắt đầu");
            }

            String title = "Sự kiện sắp bắt đầu";
            String timeStr = startTime != null 
                ? startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "sắp tới";
            String message = String.format("Sự kiện \"%s\" sẽ bắt đầu vào %s. Hãy chuẩn bị tham gia!", 
                eventTitle, timeStr);

            // Kiểm tra xem đã gửi thông báo này chưa (tránh duplicate)
            if (notificationRepository.existsByUserIdAndEventIdAndTitle(userId, eventId, title)) {
                logger.warn("⚠️ Đã gửi thông báo sự kiện sắp bắt đầu cho user {} về event {}", userId, eventId);
                return;
            }

            // Lưu vào database
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setEventId(eventId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setStatus(Notification.NotificationStatus.unread);
            
            Notification savedNotification = notificationRepository.save(notification);

            // Tạo JSON response để gửi qua WebSocket
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notificationId", savedNotification.getNotificationId());
            notificationData.put("userId", savedNotification.getUserId());
            notificationData.put("eventId", savedNotification.getEventId());
            notificationData.put("title", savedNotification.getTitle());
            notificationData.put("message", savedNotification.getMessage());
            notificationData.put("status", savedNotification.getStatus().name());
            notificationData.put("createdAt", savedNotification.getCreatedAt() != null 
                ? savedNotification.getCreatedAt().toString() 
                : java.time.LocalDateTime.now().toString());
            notificationData.put("type", "event_start_soon"); // Loại thông báo

            // Gửi thông báo realtime qua WebSocket tới topic: /topic/user/{userId}
            String destination = "/topic/user/" + userId;
            messagingTemplate.convertAndSend(destination, notificationData);
            
            logger.info("✅ Đã gửi thông báo sự kiện sắp bắt đầu WebSocket tới: {}", destination);
        } catch (Exception e) {
            // Log lỗi nhưng không throw
            logger.error("❌ Lỗi khi gửi thông báo sự kiện sắp bắt đầu WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi thông báo cho user khi sự kiện đã kết thúc (yêu cầu feedback)
     * 
     * @param userId ID của user (người nhận thông báo)
     * @param eventId ID của sự kiện
     * @param eventTitle Tên sự kiện
     */
    @Transactional
    public void sendEventEndedNotification(Long userId, Long eventId, String eventTitle) {
        try {
            // Validate input
            if (userId == null || eventId == null || eventTitle == null) {
                throw new IllegalArgumentException("Thông tin không đầy đủ để gửi thông báo sự kiện đã kết thúc");
            }

            String title = "Sự kiện đã kết thúc";
            String message = String.format("Sự kiện \"%s\" đã kết thúc. Hãy chia sẻ đánh giá của bạn để giúp chúng tôi cải thiện!", 
                eventTitle);

            // Kiểm tra xem đã gửi thông báo này chưa (tránh duplicate)
            if (notificationRepository.existsByUserIdAndEventIdAndTitle(userId, eventId, title)) {
                logger.warn("⚠️ Đã gửi thông báo sự kiện đã kết thúc cho user {} về event {}", userId, eventId);
                return;
            }

            // Lưu vào database
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setEventId(eventId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setStatus(Notification.NotificationStatus.unread);
            
            Notification savedNotification = notificationRepository.save(notification);

            // Tạo JSON response để gửi qua WebSocket
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("notificationId", savedNotification.getNotificationId());
            notificationData.put("userId", savedNotification.getUserId());
            notificationData.put("eventId", savedNotification.getEventId());
            notificationData.put("title", savedNotification.getTitle());
            notificationData.put("message", savedNotification.getMessage());
            notificationData.put("status", savedNotification.getStatus().name());
            notificationData.put("createdAt", savedNotification.getCreatedAt() != null 
                ? savedNotification.getCreatedAt().toString() 
                : java.time.LocalDateTime.now().toString());
            notificationData.put("type", "event_ended"); // Loại thông báo

            // Gửi thông báo realtime qua WebSocket tới topic: /topic/user/{userId}
            String destination = "/topic/user/" + userId;
            messagingTemplate.convertAndSend(destination, notificationData);
            
            logger.info("✅ Đã gửi thông báo sự kiện đã kết thúc WebSocket tới: {}", destination);
        } catch (Exception e) {
            // Log lỗi nhưng không throw
            logger.error("❌ Lỗi khi gửi thông báo sự kiện đã kết thúc WebSocket: {}", e.getMessage(), e);
        }
    }
}

