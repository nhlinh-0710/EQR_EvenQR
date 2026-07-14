package com.eventqr.service;

import com.eventqr.model.Event;
import com.eventqr.dto.EventRequest;
import com.eventqr.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventRepository eventRepository; 
    
    @Autowired
    private FileStorageService fileStorageService; 

    @Override
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện có ID: " + eventId));
    }

    @Override
    @Transactional
    public Event createEvent(EventRequest request) throws Exception {
        
        // 1. Xử lý File Upload (Placeholder)
        String imageUrl = handleFileUpload(request.getEventImage());
        
        // 2. Ánh xạ DTO sang Entity
        Event newEvent = new Event();
        
        // Tính toán EndTime
        LocalDateTime startTime = request.getEventDate();
        int durationHours = request.getDuration() != null ? request.getDuration() : 2; 
        LocalDateTime endTime = startTime.plusHours(durationHours);

        newEvent.setTitle(request.getTitle());
        newEvent.setDescription(request.getDescription());
        newEvent.setLocation(request.getLocation());
        newEvent.setCategory(request.getCategory());
        newEvent.setStartTime(startTime);
        newEvent.setEndTime(endTime);
        newEvent.setMaxParticipants(request.getMaxParticipants());
        
        newEvent.setImageUrl(imageUrl); 
        
        // Xác định status: nếu có status từ request thì dùng, không thì tự động xác định
        String status = request.getStatus();
        if (status == null || status.trim().isEmpty()) {
            // Tự động xác định status dựa trên thời gian
            LocalDateTime now = LocalDateTime.now();
            if (startTime != null) {
                if (endTime != null && endTime.isBefore(now)) {
                    status = "COMPLETED"; // Đã kết thúc
                } else if (startTime.isAfter(now)) {
                    status = "UPCOMING"; // Sắp diễn ra
                } else {
                    // startTime <= now và (endTime == null hoặc endTime >= now)
                    status = "ONGOING"; // Đang diễn ra
                }
            } else {
                status = "DRAFT"; // Bản nháp nếu không có thời gian
            }
        }
        newEvent.setStatus(status);
        
        // Lấy organizerId từ request (bắt buộc phải có)
        Long organizerId = request.getOrganizerId();
        if (organizerId == null) {
            throw new IllegalArgumentException("Thiếu thông tin organizerId. Không thể tạo sự kiện mà không biết người tạo.");
        }
        newEvent.setOrganizerId(organizerId); 
        newEvent.setCreatedAt(LocalDateTime.now());
        newEvent.setUpdatedAt(LocalDateTime.now());
        
        // 3. Lưu vào Database
        return eventRepository.save(newEvent);
    }

    /**
     * PHƯƠNG THỨC MỚI ĐỂ LẤY TẤT CẢ SỰ KIỆN
     */
    @Override
    public List<Event> findAll() {
        // JpaRepository cung cấp phương thức findAll() để lấy toàn bộ danh sách.
        return eventRepository.findAll(); 
    }

    /**
     * Lấy danh sách sự kiện của một organizer
     */
    @Override
    public List<Event> findByOrganizerId(Long organizerId) {
        return eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
    }

    /**
     * Kiểm tra quyền sở hữu sự kiện
     */
    @Override
    public boolean isEventOwner(Long eventId, Long organizerId) {
        return eventRepository.existsByEventIdAndOrganizerId(eventId, organizerId);
    }

    @Override
    @Transactional
    public Event updateEvent(Long eventId, EventRequest request, Long organizerId) throws Exception {
        // 1. Tìm sự kiện hiện tại
        Optional<Event> existingEventOpt = eventRepository.findById(eventId);
        if (existingEventOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy sự kiện có ID: " + eventId);
        }
        Event existingEvent = existingEventOpt.get();
        
        // 2. Kiểm tra quyền sở hữu
        if (!existingEvent.getOrganizerId().equals(organizerId)) {
            throw new IllegalStateException("Bạn không có quyền chỉnh sửa sự kiện này");
        }

        // 3. Xử lý File Upload (nếu có file mới)
        if (request.getEventImage() != null && !request.getEventImage().isEmpty()) {
            // Xóa ảnh cũ trước khi lưu ảnh mới
            String oldImageUrl = existingEvent.getImageUrl();
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                fileStorageService.deleteFile(oldImageUrl);
            }
            
            // Lưu ảnh mới
            String newImageUrl = handleFileUpload(request.getEventImage());
            existingEvent.setImageUrl(newImageUrl);
        }

        // 4. Cập nhật các trường: chỉ cập nhật nếu giá trị mới không null
        existingEvent.setTitle(request.getTitle() != null ? request.getTitle() : existingEvent.getTitle());
        existingEvent.setDescription(request.getDescription() != null ? request.getDescription() : existingEvent.getDescription());
        existingEvent.setLocation(request.getLocation() != null ? request.getLocation() : existingEvent.getLocation());
        existingEvent.setCategory(request.getCategory() != null ? request.getCategory() : existingEvent.getCategory());
        existingEvent.setMaxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : existingEvent.getMaxParticipants());
        // Cập nhật status: nếu có status từ request thì dùng, không thì giữ nguyên
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            existingEvent.setStatus(request.getStatus().trim().toUpperCase());
        }
        // Nếu request.getStatus() là null hoặc empty, giữ nguyên status hiện tại

        // Cập nhật StartTime và EndTime
        if (request.getEventDate() != null) {
            existingEvent.setStartTime(request.getEventDate());
        }
        if (request.getDuration() != null) {
            existingEvent.setEndTime(existingEvent.getStartTime().plusHours(request.getDuration()));
        }

        existingEvent.setUpdatedAt(LocalDateTime.now());
        
        // 5. Lưu cập nhật vào Database
        return eventRepository.save(existingEvent);
    }

    /**
     * Xóa sự kiện (chỉ organizer sở hữu mới được xóa)
     */
    @Override
    @Transactional
    public void deleteEvent(Long eventId, Long organizerId) throws Exception {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện có ID: " + eventId));
        
        // Kiểm tra quyền sở hữu
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new IllegalStateException("Bạn không có quyền xóa sự kiện này");
        }
        
        // Xóa ảnh trước khi xóa sự kiện
        String imageUrl = event.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            fileStorageService.deleteFile(imageUrl);
        }
        
        eventRepository.delete(event);
    }
    
    /**
     * Xử lý upload file ảnh sự kiện
     */
    private String handleFileUpload(MultipartFile file) throws Exception {
        return fileStorageService.saveFile(file);
    }

    /**
     * Cập nhật status của event dựa trên thời gian hiện tại (KHÔNG lưu DB)
     * Không override status nếu là CANCELLED hoặc DRAFT
     */
    public static void updateEventStatus(Event event, LocalDateTime now) {
        String currentStatus = event.getStatus();
        if (currentStatus != null) {
            String upperStatus = currentStatus.toUpperCase();
            if ("CANCELLED".equals(upperStatus) || "DRAFT".equals(upperStatus)) {
                return;
            }
        }
        LocalDateTime startTime = event.getStartTime();
        LocalDateTime endTime = event.getEndTime();
        if (startTime == null || endTime == null) {
            event.setStatus("DRAFT");
            return;
        }
        if (now.isBefore(startTime)) {
            event.setStatus("UPCOMING");
        } else if (now.isAfter(endTime)) {
            event.setStatus("COMPLETED");
        } else {
            event.setStatus("ONGOING");
        }
    }
}