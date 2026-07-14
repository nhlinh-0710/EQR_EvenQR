package com.eventqr.service;

import com.eventqr.dto.AttendeeResponse;
import com.eventqr.dto.AttendeeStatistics;
import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;
import com.eventqr.repository.CheckinRepository;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendeeService {
    
    @Autowired
    private EventTicketRepository ticketRepository;
    
    @Autowired
    private CheckinRepository checkinRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    /**
     * Lấy danh sách người tham dự theo eventId
     */
    @Transactional(readOnly = true)
    public List<AttendeeResponse> getAttendeesByEventId(Long eventId, Long organizerId) {
        // Kiểm tra quyền sở hữu event
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Sự kiện không tồn tại"));
        
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalStateException("Bạn không có quyền xem danh sách người tham dự của sự kiện này");
        }
        
        // Lấy tất cả tickets của event
        List<EventTicket> tickets = ticketRepository.findByEventId(eventId);
        
        // Convert sang AttendeeResponse
        return tickets.stream()
            .map(ticket -> {
                // Xác định status
                String status;
                if (ticket.getCancelled() != null && ticket.getCancelled()) {
                    status = "CANCELLED";
                } else if (checkinRepository.existsByEventIdAndUserId(eventId, ticket.getUserId())) {
                    status = "CHECKED_IN";
                } else {
                    status = "REGISTERED";
                }
                
                return new AttendeeResponse(
                    ticket.getTicketId(),
                    ticket.getUserId(),
                    ticket.getName(),
                    ticket.getEmail(),
                    ticket.getPhone(),
                    ticket.getOccupation(),
                    ticket.getTicketType(),
                    ticket.getNote(),
                    ticket.getRegisteredAt(),
                    status,
                    null,  // CheckedInAt - có thể cải thiện sau
                    ticket.getCancelled()
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy thống kê người tham dự cho một event
     */
    @Transactional(readOnly = true)
    public AttendeeStatistics getAttendeeStatistics(Long eventId, Long organizerId) {
        // Kiểm tra quyền sở hữu event
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Sự kiện không tồn tại"));
        
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalStateException("Bạn không có quyền xem thống kê của sự kiện này");
        }
        
        // Lấy tất cả tickets
        List<EventTicket> tickets = ticketRepository.findByEventId(eventId);
        
        // Tính toán thống kê
        int totalCancelled = (int) tickets.stream()
            .filter(t -> t.getCancelled() != null && t.getCancelled())
            .count();
        
        int totalRegistered = (int) tickets.stream()
            .filter(t -> t.getCancelled() == null || !t.getCancelled())
            .count();
        
        int totalCheckedIn = checkinRepository.countByEventId(eventId);
        
        int totalPending = totalRegistered - totalCheckedIn;
        
        return new AttendeeStatistics(
            eventId,
            event.getTitle(),
            totalRegistered,
            totalCheckedIn,
            totalPending,
            totalCancelled
        );
    }
    
    /**
     * Lấy tổng thống kê cho tất cả events của organizer
     */
    @Transactional(readOnly = true)
    public AttendeeStatistics getAllEventsStatistics(Long organizerId) {
        // Lấy tất cả events của organizer
        List<Event> events = eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
        
        int totalRegistered = 0;
        int totalCheckedIn = 0;
        int totalCancelled = 0;
        
        for (Event event : events) {
            List<EventTicket> tickets = ticketRepository.findByEventId(event.getEventId());
            
            totalCancelled += tickets.stream()
                .filter(t -> t.getCancelled() != null && t.getCancelled())
                .count();
            
            totalRegistered += tickets.stream()
                .filter(t -> t.getCancelled() == null || !t.getCancelled())
                .count();
            
            totalCheckedIn += checkinRepository.countByEventId(event.getEventId());
        }
        
        int totalPending = totalRegistered - totalCheckedIn;
        
        return new AttendeeStatistics(
            null,
            "Tất cả sự kiện",
            totalRegistered,
            totalCheckedIn,
            totalPending,
            totalCancelled
        );
    }
}

