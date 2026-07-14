package com.eventqr.service;

import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduled service để gửi thông báo tự động cho users:
 * 1. Thông báo khi sự kiện sắp bắt đầu (1 giờ trước)
 * 2. Thông báo khi sự kiện đã kết thúc (yêu cầu feedback)
 */
@Service
public class EventNotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EventNotificationScheduler.class);

    private final EventRepository eventRepository;
    private final EventTicketRepository eventTicketRepository;
    private final NotificationService notificationService;

    @Autowired
    public EventNotificationScheduler(EventRepository eventRepository,
                                     EventTicketRepository eventTicketRepository,
                                     NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.eventTicketRepository = eventTicketRepository;
        this.notificationService = notificationService;
    }

    /**
     * Chạy mỗi 15 phút để kiểm tra và gửi thông báo cho các sự kiện sắp bắt đầu
     * Cron: 0 0/15 * * * ? = mỗi 15 phút
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    @Transactional
    public void notifyUsersAboutEventsStartingSoon() {
        try {
            logger.info("🔔 Bắt đầu kiểm tra sự kiện sắp bắt đầu...");
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);
            
            // Tìm các sự kiện sắp bắt đầu (trong khoảng 1 giờ tới)
            List<Event> eventsStartingSoon = eventRepository.findEventsStartingSoon(now, oneHourLater);
            
            if (eventsStartingSoon.isEmpty()) {
                logger.info("📭 Không có sự kiện nào sắp bắt đầu");
                return;
            }
            
            logger.info("📅 Tìm thấy {} sự kiện sắp bắt đầu", eventsStartingSoon.size());
            
            int totalNotifications = 0;
            
            for (Event event : eventsStartingSoon) {
                try {
                    // Lấy danh sách users đã đăng ký sự kiện này
                    List<EventTicket> tickets = eventTicketRepository.findByEventId(event.getEventId())
                        .stream()
                        .filter(t -> !t.getCancelled())
                        .collect(Collectors.toList());
                    
                    // Lấy danh sách userId từ tickets
                    List<Long> userIds = tickets.stream()
                        .map(EventTicket::getUserId)
                        .distinct()
                        .collect(Collectors.toList());
                    
                    logger.info("🎫 Sự kiện \"{}\" (ID: {}) có {} người đăng ký", 
                        event.getTitle(), event.getEventId(), userIds.size());
                    
                    // Gửi thông báo cho từng user
                    for (Long userId : userIds) {
                        try {
                            notificationService.sendEventStartSoonNotification(
                                userId,
                                event.getEventId(),
                                event.getTitle(),
                                event.getStartTime()
                            );
                            totalNotifications++;
                        } catch (Exception e) {
                            logger.error("❌ Lỗi khi gửi thông báo cho user {} về event {}: {}", 
                                userId, event.getEventId(), e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.error("❌ Lỗi khi xử lý event {}: {}", event.getEventId(), e.getMessage());
                }
            }
            
            logger.info("✅ Đã gửi {} thông báo về sự kiện sắp bắt đầu", totalNotifications);
        } catch (Exception e) {
            logger.error("❌ Lỗi trong scheduled task notifyUsersAboutEventsStartingSoon: {}", e.getMessage(), e);
        }
    }

    /**
     * Chạy mỗi 15 phút để kiểm tra và gửi thông báo cho các sự kiện vừa kết thúc
     * Cron: 0 0/15 * * * ? = mỗi 15 phút
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    @Transactional
    public void notifyUsersAboutEventsEnded() {
        try {
            logger.info("🔔 Bắt đầu kiểm tra sự kiện đã kết thúc...");
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);
            
            // Tìm các sự kiện vừa kết thúc (trong vòng 1 giờ qua)
            List<Event> eventsJustEnded = eventRepository.findEventsJustEnded(oneHourAgo, now);
            
            if (eventsJustEnded.isEmpty()) {
                logger.info("📭 Không có sự kiện nào vừa kết thúc");
                return;
            }
            
            logger.info("📅 Tìm thấy {} sự kiện vừa kết thúc", eventsJustEnded.size());
            
            int totalNotifications = 0;
            
            for (Event event : eventsJustEnded) {
                try {
                    // Lấy danh sách users đã đăng ký sự kiện này
                    List<EventTicket> tickets = eventTicketRepository.findByEventId(event.getEventId())
                        .stream()
                        .filter(t -> !t.getCancelled())
                        .collect(Collectors.toList());
                    
                    // Lấy danh sách userId từ tickets
                    List<Long> userIds = tickets.stream()
                        .map(EventTicket::getUserId)
                        .distinct()
                        .collect(Collectors.toList());
                    
                    logger.info("🎫 Sự kiện \"{}\" (ID: {}) đã kết thúc, có {} người đăng ký", 
                        event.getTitle(), event.getEventId(), userIds.size());
                    
                    // Gửi thông báo cho từng user
                    for (Long userId : userIds) {
                        try {
                            notificationService.sendEventEndedNotification(
                                userId,
                                event.getEventId(),
                                event.getTitle()
                            );
                            totalNotifications++;
                        } catch (Exception e) {
                            logger.error("❌ Lỗi khi gửi thông báo cho user {} về event {}: {}", 
                                userId, event.getEventId(), e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.error("❌ Lỗi khi xử lý event {}: {}", event.getEventId(), e.getMessage());
                }
            }
            
            logger.info("✅ Đã gửi {} thông báo về sự kiện đã kết thúc", totalNotifications);
        } catch (Exception e) {
            logger.error("❌ Lỗi trong scheduled task notifyUsersAboutEventsEnded: {}", e.getMessage(), e);
        }
    }
}

