package com.eventqr.controller;

import com.eventqr.dto.EventStatisticsDTO;
import com.eventqr.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * GET /api/statistics/organizer/{organizerId}
     * Lấy thống kê tổng quan cho organizer
     */
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<?> getOrganizerStatistics(@PathVariable Long organizerId) {
        try {
            EventStatisticsDTO statistics = statisticsService.getOrganizerStatistics(organizerId);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Lỗi khi lấy thống kê: " + e.getMessage())
            );
        }
    }
}

