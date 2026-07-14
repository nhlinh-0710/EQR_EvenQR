package com.eventqr.dto;

/**
 * DTO để trả về thống kê dashboard
 */
public class DashboardStatsDTO {
    
    private long activeEvents;       // Số sự kiện hoạt động
    private long totalAttendees;     // Tổng số người tham dự (đã check-in)
    private long totalTicketsSold;   // Tổng số vé đã bán
    private long totalRevenue;       // Tổng doanh thu (VND) - tạm tính mẫu
    
    public DashboardStatsDTO() {}
    
    public DashboardStatsDTO(long activeEvents, long totalAttendees, long totalTicketsSold, long totalRevenue) {
        this.activeEvents = activeEvents;
        this.totalAttendees = totalAttendees;
        this.totalTicketsSold = totalTicketsSold;
        this.totalRevenue = totalRevenue;
    }
    
    // Getters and Setters
    public long getActiveEvents() {
        return activeEvents;
    }
    
    public void setActiveEvents(long activeEvents) {
        this.activeEvents = activeEvents;
    }
    
    public long getTotalAttendees() {
        return totalAttendees;
    }
    
    public void setTotalAttendees(long totalAttendees) {
        this.totalAttendees = totalAttendees;
    }
    
    public long getTotalTicketsSold() {
        return totalTicketsSold;
    }
    
    public void setTotalTicketsSold(long totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }
    
    public long getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}

