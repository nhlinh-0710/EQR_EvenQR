package com.eventqr.controller;

import com.eventqr.dto.OrganizerFeedbackResponse;
import com.eventqr.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organizer")
public class OrganizerController {

    private final FeedbackService feedbackService;

    public OrganizerController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/{id}/feedback")
    public ResponseEntity<?> getOrganizerFeedbacks(@PathVariable("id") Long organizerId) {
        try {
            List<OrganizerFeedbackResponse> feedbacks = feedbackService.getOrganizerFeedbacks(organizerId);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
