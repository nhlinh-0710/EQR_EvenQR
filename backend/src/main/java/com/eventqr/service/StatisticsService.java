package com.eventqr.service;

import com.eventqr.dto.EventStatisticsDTO;
import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    private final EventRepository eventRepository;
    private final EventTicketRepository eventTicketRepository;

    @Autowired
    public StatisticsService(EventRepository eventRepository,
                            EventTicketRepository eventTicketRepository) {
        this.eventRepository = eventRepository;
        this.eventTicketRepository = eventTicketRepository;
    }

    /**
     * Lấy thống kê tổng quan cho organizer
     */
    // LINH
    @Transactional(readOnly = true)
    public EventStatisticsDTO getOrganizerStatistics(Long organizerId) {
        logger.info("📊 Lấy thống kê cho organizer: {}", organizerId);

        // 1. Lấy tất cả events của organizer
        List<Event> events = eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
        int totalEvents = events.size();

        if (totalEvents == 0) {
            logger.info("⚠️ Organizer {} chưa có sự kiện nào", organizerId);
            return new EventStatisticsDTO(organizerId, 0, 0);
        }

        List<Long> eventIds = events.stream()
            .map(Event::getEventId)
            .collect(Collectors.toList());

        // 2. Đếm tổng số đăng ký (tickets)
        int totalRegistrations = 0;
        for (Long eventId : eventIds) {
            List<EventTicket> tickets = eventTicketRepository.findByEventId(eventId);
            totalRegistrations += tickets.stream()
                .filter(t -> !t.getCancelled())
                .count();
        }

        // 3. Tạo DTO
        EventStatisticsDTO stats = new EventStatisticsDTO(
            organizerId,
            totalEvents,
            totalRegistrations
        );

        // 4. Thống kê từng sự kiện
        List<EventStatisticsDTO.EventRegistrationStat> eventStats = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Event event : events) {
            List<EventTicket> tickets = eventTicketRepository.findByEventId(event.getEventId());
            int regCount = (int) tickets.stream().filter(t -> !t.getCancelled()).count();
            String eventDate = event.getStartTime() != null 
                ? event.getStartTime().format(dateFormatter) 
                : "Chưa xác định";

            EventStatisticsDTO.EventRegistrationStat eventStat = 
                new EventStatisticsDTO.EventRegistrationStat(
                    event.getEventId(),
                    event.getTitle(),
                    regCount,
                    event.getStatus(),
                    eventDate
                );
            eventStats.add(eventStat);
        }
        stats.setEventStats(eventStats);

        // 5. Thống kê theo thời gian (6 tháng gần nhất)
        List<EventStatisticsDTO.TimeSeriesStat> timeSeriesStats = calculateTimeSeriesStats(organizerId, eventIds);
        stats.setTimeSeriesStats(timeSeriesStats);

        logger.info("✅ Thống kê organizer {}: {} events, {} registrations", 
            organizerId, totalEvents, totalRegistrations);

        return stats;
    }
    // LINH

    /**
     * Tính thống kê theo chuỗi thời gian (6 tháng gần nhất)
     */
    private List<EventStatisticsDTO.TimeSeriesStat> calculateTimeSeriesStats(Long organizerId, List<Long> eventIds) {
        List<EventStatisticsDTO.TimeSeriesStat> result = new ArrayList<>();
        
        // Tạo danh sách 6 tháng gần nhất
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        Map<String, Integer> monthlyStats = new LinkedHashMap<>();
        
        // Khởi tạo 6 tháng gần nhất với giá trị 0
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i);
            String monthKey = monthDate.format(monthFormatter);
            monthlyStats.put(monthKey, 0);
        }
        
        // Đếm số đăng ký theo tháng
        for (Long eventId : eventIds) {
            List<EventTicket> tickets = eventTicketRepository.findByEventId(eventId);
            
            for (EventTicket ticket : tickets) {
                if (!ticket.getCancelled() && ticket.getRegisteredAt() != null) {
                    String monthKey = ticket.getRegisteredAt().format(monthFormatter);
                    
                    // Chỉ tính trong 6 tháng gần nhất
                    if (monthlyStats.containsKey(monthKey)) {
                        monthlyStats.put(monthKey, monthlyStats.get(monthKey) + 1);
                    }
                }
            }
        }
        
        // Chuyển map thành list
        for (Map.Entry<String, Integer> entry : monthlyStats.entrySet()) {
            result.add(new EventStatisticsDTO.TimeSeriesStat(entry.getKey(), entry.getValue()));
        }
        
        return result;
    }
}

