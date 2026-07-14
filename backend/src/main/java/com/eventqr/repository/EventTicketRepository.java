package com.eventqr.repository;

import com.eventqr.model.EventTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventTicketRepository extends JpaRepository<EventTicket, Long> {

    boolean existsByEventIdAndUserId(Long eventId, Long userId);  // 🔥 Long, Long

    List<EventTicket> findByUserId(Long userId);                  // 🔥 Long
    
    List<EventTicket> findByEventId(Long eventId);                 // Tìm tickets theo eventId

        /**
     * Xoá tất cả vé của một event
     */
    void deleteByEventId(Long eventId);

}
