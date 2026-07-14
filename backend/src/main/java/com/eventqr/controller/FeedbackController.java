package com.eventqr.controller;

import com.eventqr.dto.CompletedEventResponse;
import com.eventqr.dto.FeedbackReplyRequest;
import com.eventqr.dto.FeedbackRequest;
import com.eventqr.dto.FeedbackResponse;
import com.eventqr.model.Feedback;
import com.eventqr.repository.FeedbackRepository;
import com.eventqr.service.FeedbackService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    private final FeedbackService feedbackService;
    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackService feedbackService, FeedbackRepository feedbackRepository) {
        this.feedbackService = feedbackService;
        this.feedbackRepository = feedbackRepository;
    }

    @GetMapping("/feedback/completed-events/{userId}")
    public ResponseEntity<?> getCompletedEvents(@PathVariable Long userId) {
        try {
            List<CompletedEventResponse> events = feedbackService.getCompletedEventsForUser(userId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest request) {
        try {
            FeedbackResponse response = feedbackService.submitFeedback(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cảm ơn bạn đã đánh giá!", "feedback", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi server: " + e.getMessage()));
        }
    }

    @PostMapping("/event/{eventId}/feedback")
    public ResponseEntity<?> submitFeedbackForEvent(@PathVariable Long eventId, @RequestBody Map<String, Object> body) {
        try {
            FeedbackRequest request = new FeedbackRequest();
            request.setEventId(eventId);
            Object userIdObj = body.get("userId");
            if (userIdObj == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Thiếu thông tin userId"));
            }
            request.setUserId(Long.valueOf(userIdObj.toString()));
            Object ratingObj = body.get("rating");
            if (ratingObj == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Thiếu thông tin rating"));
            }
            request.setRating(Integer.valueOf(ratingObj.toString()));
            Object commentObj = body.get("comment");
            if (commentObj != null) request.setComment(commentObj.toString());

            FeedbackResponse response = feedbackService.submitFeedback(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cảm ơn bạn đã đánh giá!", "feedback", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Lỗi khi tạo feedback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/feedback/event/{eventId}")
    public ResponseEntity<?> getFeedbacksByEvent(@PathVariable Long eventId) {
        try {
            List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByEventId(eventId);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/feedback/user/{userId}")
    public ResponseEntity<?> getFeedbacksByUser(@PathVariable Long userId) {
        try {
            List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByUserId(userId);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/feedback/organizer/{organizerId}")
    public ResponseEntity<?> getFeedbacksByOrganizer(@PathVariable Long organizerId) {
        try {
            List<FeedbackResponse> feedbacks = feedbackService.getFeedbacksByOrganizerId(organizerId);
            return ResponseEntity.ok(feedbacks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/feedback/{feedbackId}/reply")
    public ResponseEntity<?> replyFeedback(@PathVariable Long feedbackId, @RequestBody FeedbackReplyRequest request) {
        try {
            if (request.getOrganizerId() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin organizerId"));
            }
            FeedbackResponse response = feedbackService.replyFeedback(feedbackId, request.getOrganizerId(), request.getReply());
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi phản hồi thành công!", "feedback", response));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/feedback/admin/event/{eventId}/export-pdf")
    public ResponseEntity<byte[]> exportFeedbacksPdf(@PathVariable Long eventId) {
        try {
            List<Feedback> feedbacks = feedbackRepository.findByEventIdOrderByCreatedAtDesc(eventId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("BÁO CÁO FEEDBACK SỰ KIỆN #" + eventId, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            if (feedbacks.isEmpty()) {
                document.add(new Paragraph("Không có feedback nào cho sự kiện này."));
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2.5f, 1.5f, 1.0f, 5.0f});

                Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
                table.addCell(new Phrase("Thời gian", headerFont));
                table.addCell(new Phrase("User ID", headerFont));
                table.addCell(new Phrase("Điểm", headerFont));
                table.addCell(new Phrase("Nội dung", headerFont));

                Font cellFont = new Font(Font.HELVETICA, 10);
                for (Feedback f : feedbacks) {
                    table.addCell(new Phrase(f.getCreatedAt() != null ? f.getCreatedAt().format(formatter) : "", cellFont));
                    table.addCell(new Phrase(f.getUserId() != null ? String.valueOf(f.getUserId()) : "", cellFont));
                    table.addCell(new Phrase(f.getRating() != null ? String.valueOf(f.getRating()) : "", cellFont));
                    table.addCell(new Phrase(f.getComment() != null ? f.getComment() : "", cellFont));
                }
                document.add(table);
            }
            document.close();

            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "feedback_event_" + eventId + ".pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Lỗi khi tạo PDF: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
