package com.eventqr.dto;

import java.util.List;

/**
 * DTO chứa thống kê tổng quan cho organizer
 */
public class EventStatisticsDTO {
    
    private Long organizerId;
    private int totalEvents;              // Tổng số sự kiện
    private int totalRegistrations;       // Tổng số đăng ký
    
    // Thống kê theo sự kiện
    private List<EventRegistrationStat> eventStats;
    
    // Thống kê theo thời gian
    private List<TimeSeriesStat> timeSeriesStats;
    
    // Constructor
    public EventStatisticsDTO() {
    }
    
    public EventStatisticsDTO(Long organizerId, int totalEvents, int totalRegistrations) {
        this.organizerId = organizerId;
        this.totalEvents = totalEvents;
        this.totalRegistrations = totalRegistrations;
    }
    
    // Getters and Setters
    public Long getOrganizerId() {
        return organizerId;
    }
    
    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }
    
    public int getTotalEvents() {
        return totalEvents;
    }
    
    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }
    
    public int getTotalRegistrations() {
        return totalRegistrations;
    }
    
    public void setTotalRegistrations(int totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }
    
    
    public List<EventRegistrationStat> getEventStats() {
        return eventStats;
    }
    
    public void setEventStats(List<EventRegistrationStat> eventStats) {
        this.eventStats = eventStats;
    }
    
    public List<TimeSeriesStat> getTimeSeriesStats() {
        return timeSeriesStats;
    }
    
    public void setTimeSeriesStats(List<TimeSeriesStat> timeSeriesStats) {
        this.timeSeriesStats = timeSeriesStats;
    }
    
    /**
     * Thống kê từng sự kiện
     */
    public static class EventRegistrationStat {
        private Long eventId;
        private String eventTitle;
        private int registrationCount;     // Số lượng đăng ký
        private String eventStatus;
        private String eventDate;          // Ngày sự kiện
        
        public EventRegistrationStat() {
        }
        
        public EventRegistrationStat(Long eventId, String eventTitle, int registrationCount, 
                                    String eventStatus, String eventDate) {
            this.eventId = eventId;
            this.eventTitle = eventTitle;
            this.registrationCount = registrationCount;
            this.eventStatus = eventStatus;
            this.eventDate = eventDate;
        }
        
        // Getters and Setters
        public Long getEventId() {
            return eventId;
        }
        
        public void setEventId(Long eventId) {
            this.eventId = eventId;
        }
        
        public String getEventTitle() {
            return eventTitle;
        }
        
        public void setEventTitle(String eventTitle) {
            this.eventTitle = eventTitle;
        }
        
        public int getRegistrationCount() {
            return registrationCount;
        }
        
        public void setRegistrationCount(int registrationCount) {
            this.registrationCount = registrationCount;
        }
        
        
        public String getEventStatus() {
            return eventStatus;
        }
        
        public void setEventStatus(String eventStatus) {
            this.eventStatus = eventStatus;
        }
        
        public String getEventDate() {
            return eventDate;
        }
        
        public void setEventDate(String eventDate) {
            this.eventDate = eventDate;
        }
    }
    
    /**
     * Thống kê theo chuỗi thời gian (theo tháng)
     */
    public static class TimeSeriesStat {
        private String period;             // Tháng/Năm (VD: "2024-01")
        private int registrationCount;     // Số đăng ký trong tháng
        
        public TimeSeriesStat() {
        }
        
        public TimeSeriesStat(String period, int registrationCount) {
            this.period = period;
            this.registrationCount = registrationCount;
        }
        
        // Getters and Setters
        public String getPeriod() {
            return period;
        }
        
        public void setPeriod(String period) {
            this.period = period;
        }
        
        public int getRegistrationCount() {
            return registrationCount;
        }
        
        public void setRegistrationCount(int registrationCount) {
            this.registrationCount = registrationCount;
        }
    }
}

