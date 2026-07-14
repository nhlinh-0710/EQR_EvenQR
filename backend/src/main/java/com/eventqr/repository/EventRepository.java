package com.eventqr.repository;

import com.eventqr.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Spring Data JPA cung cấp các hàm CRUD cơ bản
    
    // Tìm tất cả sự kiện của một organizer
    List<Event> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);
    
    // Kiểm tra sự kiện có thuộc về organizer không
    boolean existsByEventIdAndOrganizerId(Long eventId, Long organizerId);
    
    // Tìm các sự kiện sắp bắt đầu (trong khoảng 1 giờ tới, nhưng chưa bắt đầu)
    @Query("SELECT e FROM Event e WHERE e.startTime >= :now AND e.startTime <= :oneHourLater AND e.status != 'CANCELLED'")
    List<Event> findEventsStartingSoon(@Param("now") LocalDateTime now, @Param("oneHourLater") LocalDateTime oneHourLater);
    
    // Tìm các sự kiện vừa kết thúc (trong vòng 1 giờ qua, nhưng đã kết thúc)
    @Query("SELECT e FROM Event e WHERE e.endTime >= :oneHourAgo AND e.endTime <= :now AND e.status != 'CANCELLED'")
    List<Event> findEventsJustEnded(@Param("oneHourAgo") LocalDateTime oneHourAgo, @Param("now") LocalDateTime now);
}

