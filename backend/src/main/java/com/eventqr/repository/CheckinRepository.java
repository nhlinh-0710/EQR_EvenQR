package com.eventqr.repository;

import com.eventqr.model.CheckInHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CheckinRepository extends JpaRepository<CheckInHistory, Long> {

    boolean existsByTicket_TicketId(Long ticketId);

    /**
     * Kiểm tra user đã check-in vào sự kiện chưa
     */
    @Query("SELECT COUNT(c) > 0 FROM CheckInHistory c WHERE c.eventId = :eventId AND c.userId = :userId")
    boolean existsByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);
    
    /**
     * Đếm số check-in của một sự kiện
     */
    @Query("SELECT COUNT(c) FROM CheckInHistory c WHERE c.eventId = :eventId")
    int countByEventId(@Param("eventId") Long eventId);
    
    /**
     * Lấy tất cả check-in của các sự kiện thuộc về organizer
     * Join với Event để lọc theo organizerId
     */
    @Query("SELECT c FROM CheckInHistory c " +
           "JOIN Event e ON c.eventId = e.eventId " +
           "WHERE e.organizerId = :organizerId " +
           "ORDER BY c.checkedAt DESC")
    List<CheckInHistory> findByOrganizerId(@Param("organizerId") Long organizerId);
    
    /**
     * Lấy check-in của một sự kiện cụ thể (kèm verify organizer)
     */
    @Query("SELECT c FROM CheckInHistory c " +
           "JOIN Event e ON c.eventId = e.eventId " +
           "WHERE c.eventId = :eventId AND e.organizerId = :organizerId " +
           "ORDER BY c.checkedAt DESC")
    List<CheckInHistory> findByEventIdAndOrganizerId(@Param("eventId") Long eventId, @Param("organizerId") Long organizerId);
    
    /**
     * Lấy tất cả check-in của một sự kiện
     */
    List<CheckInHistory> findByEventId(Long eventId);

        /**
     * Xoá toàn bộ lịch sử checkin của một event
     */
    void deleteByEventId(Long eventId);

}

