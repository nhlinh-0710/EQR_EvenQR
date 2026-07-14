package com.eventqr.repository;

import com.eventqr.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {


    /**
     * Tìm feedback theo eventId và userId (để kiểm tra user đã feedback chưa)
     */
    Optional<Feedback> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Lấy tất cả feedback của một sự kiện
     */
    List<Feedback> findByEventIdOrderByCreatedAtDesc(Long eventId);

        /**
     * Xoá tất cả feedback của một event
     */
    void deleteByEventId(Long eventId);

    /**
     * Lấy tất cả feedback của một user
     */
    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Kiểm tra user đã feedback sự kiện chưa
     */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Lấy danh sách eventId mà user đã feedback
     */
    @Query("SELECT f.eventId FROM Feedback f WHERE f.userId = :userId")
    List<Long> findEventIdsByUserId(@Param("userId") Long userId);

    /**
     * Lấy tất cả feedback của các sự kiện thuộc về một organizer
     * Sử dụng INNER JOIN để đảm bảo chỉ lấy feedback của event thuộc về organizer
     */
    @Query("SELECT f FROM Feedback f INNER JOIN Event e ON f.eventId = e.eventId WHERE e.organizerId = :organizerId ORDER BY f.createdAt DESC")
    List<Feedback> findByOrganizerIdOrderByCreatedAtDesc(@Param("organizerId") Long organizerId);
    
    /**
     * Lấy feedback theo danh sách event IDs
     */
    @Query("SELECT f FROM Feedback f WHERE f.eventId IN :eventIds ORDER BY f.createdAt DESC")
    List<Feedback> findByEventIdInOrderByCreatedAtDesc(@Param("eventIds") List<Long> eventIds);
}

