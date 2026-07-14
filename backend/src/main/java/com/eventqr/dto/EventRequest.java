package com.eventqr.dto; // <--- Đảm bảo đúng package

import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

public class EventRequest {
    
    // Lưu ý: Các tên biến này phải khớp với tên field trong FormData của Frontend (events.js, create-event.js)
    
    private Long id; 
    private String title;
    private String category;
    
    // Đây là trường nhận ngày giờ tổ chức từ input HTML <input type="datetime-local">
    private LocalDateTime eventDate; 
    
    // Thời lượng (tính bằng giờ)
    private Integer duration; 
    
    private String location;
    private Integer maxParticipants;
    private String description;
    
    // Trường quan trọng nhất: Nhận file ảnh từ form multipart/form-data
    private MultipartFile eventImage; 
    private String status; 
    private Long organizerId; // ID của organizer tạo sự kiện
    
    // --- Getters và Setters (Bắt buộc phải có để Spring Framework hoạt động) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public MultipartFile getEventImage() { return eventImage; }
    public void setEventImage(MultipartFile eventImage) { this.eventImage = eventImage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOrganizerId() { return organizerId; }
    public void setOrganizerId(Long organizerId) { this.organizerId = organizerId; }
}