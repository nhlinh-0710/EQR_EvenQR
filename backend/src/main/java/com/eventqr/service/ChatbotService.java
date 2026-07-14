package com.eventqr.service;

import com.eventqr.dto.ChatbotRequest;
import com.eventqr.dto.ChatbotResponse;
import com.eventqr.model.Event;
import com.eventqr.repository.EventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Autowired
    private EventRepository eventRepository;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatbotResponse processMessage(ChatbotRequest request) {
        try {
            // Nếu không có API key, dùng intelligent fallback
            if (geminiApiKey == null || geminiApiKey.isEmpty()) {
                return getIntelligentResponse(request.getMessage());
            }

            // Lấy thông tin sự kiện để làm context
            String eventContext = buildEventContext();

            // Xây dựng prompt với context về sự kiện
            String prompt = buildEnhancedPrompt(request.getMessage(), eventContext);

            // Gọi Google Gemini API
            String aiResponse = callGeminiAPI(prompt);

            return new ChatbotResponse(true, aiResponse, request.getConversationId(), null);

        } catch (Exception e) {
            logger.error("❌ Lỗi khi xử lý chatbot message: {}", e.getMessage(), e);
            // Fallback to intelligent response
            return getIntelligentResponse(request.getMessage());
        }
    }

    private String buildEventContext() {
        try {
            List<Event> upcomingEvents = eventRepository.findAll()
                .stream()
                .filter(e -> {
                    if (e.getStartTime() == null) return false;
                    if (e.getStatus() != null && e.getStatus().equalsIgnoreCase("CANCELLED")) return false;
                    return e.getStartTime().isAfter(LocalDateTime.now());
                })
                .limit(20)
                .collect(Collectors.toList());

            if (upcomingEvents.isEmpty()) {
                return "Hiện tại không có sự kiện sắp diễn ra.";
            }

            StringBuilder context = new StringBuilder("Danh sách sự kiện sắp diễn ra:\n");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Event event : upcomingEvents) {
                context.append(String.format(
                    "- %s (ID: %d): %s tại %s, ngày %s. Danh mục: %s. Số lượng tối đa: %d\n",
                    event.getTitle(),
                    event.getEventId(),
                    event.getDescription() != null ? event.getDescription().substring(0, Math.min(100, event.getDescription().length())) : "Không có mô tả",
                    event.getLocation(),
                    event.getStartTime() != null ? event.getStartTime().format(formatter) : "Chưa xác định",
                    event.getCategory() != null ? event.getCategory() : "N/A",
                    event.getMaxParticipants() != null ? event.getMaxParticipants() : 0
                ));
            }

            return context.toString();
        } catch (Exception e) {
            return "Không thể lấy thông tin sự kiện.";
        }
    }

    private String buildEnhancedPrompt(String userMessage, String eventContext) {
        return String.format(
            "Bạn là trợ lý AI thông minh và thân thiện của hệ thống quản lý sự kiện EventQR. " +
            "Bạn có khả năng hiểu ngữ cảnh và trả lời câu hỏi một cách tự nhiên.\n\n" +
            "THÔNG TIN SỰ KIỆN HIỆN CÓ:\n%s\n\n" +
            "NHIỆM VỤ:\n" +
            "1. Trả lời câu hỏi về sự kiện một cách chính xác và chi tiết\n" +
            "2. Gợi ý sự kiện phù hợp dựa trên câu hỏi của người dùng\n" +
            "3. Hướng dẫn cách đăng ký, check-in, và sử dụng hệ thống\n" +
            "4. Trả lời bằng tiếng Việt, thân thiện và tự nhiên\n\n" +
            "CÂU HỎI CỦA NGƯỜI DÙNG: %s\n\n" +
            "Hãy trả lời một cách thông minh, ngắn gọn (150-250 từ) và hữu ích. " +
            "Nếu có thể, hãy đề cập đến các sự kiện cụ thể từ danh sách trên.",
            eventContext,
            userMessage
        );
    }

    private String callGeminiAPI(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(Map.of("text", prompt)));

        requestBody.put("contents", List.of(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = geminiApiUrl + "?key=" + geminiApiKey;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            JsonNode candidates = jsonNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode contentNode = candidates.get(0).get("content");
                if (contentNode != null) {
                    JsonNode parts = contentNode.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }
        }

        throw new Exception("Không thể nhận phản hồi từ AI");
    }

    /**
     * Intelligent response system với NLP đơn giản
     */
    private ChatbotResponse getIntelligentResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase().trim();
        
        // Intent Recognition
        Intent intent = recognizeIntent(lowerMessage);
        
        switch (intent) {
            case GREETING:
                return handleGreeting();
            case SEARCH_BY_LOCATION:
                return searchEventsByLocation(userMessage);
            case SEARCH_BY_CATEGORY:
                return searchEventsByCategory(userMessage);
            case SEARCH_BY_TIME:
                return searchEventsByTime(userMessage);
            case SEARCH_BY_NAME:
                return searchEventsByName(userMessage);
            case REGISTRATION_HELP:
                return handleRegistrationHelp();
            case QR_CHECKIN_HELP:
                return handleQRCheckinHelp();
            case EVENT_COUNT:
                return handleEventCount();
            case GENERAL_SEARCH:
                return searchUpcomingEvents(userMessage);
            default:
                return handleGeneralQuery(userMessage);
        }
    }

    private enum Intent {
        GREETING, SEARCH_BY_LOCATION, SEARCH_BY_CATEGORY, SEARCH_BY_TIME,
        SEARCH_BY_NAME, REGISTRATION_HELP, QR_CHECKIN_HELP, EVENT_COUNT,
        GENERAL_SEARCH, UNKNOWN
    }

    private Intent recognizeIntent(String message) {
        // Greeting - check first
        if (message.matches(".*(xin chào|hello|hi|chào|chào bạn|hey).*")) {
            return Intent.GREETING;
        }
        
        // Location search - check BEFORE general search (higher priority)
        // Check for location keywords first
        String[] locationKeywords = {
            "đà nẵng", "da nang", "danang", "hà nội", "ha noi", "hanoi",
            "hồ chí minh", "ho chi minh", "hcm", "sài gòn", "sai gon",
            "quang trung", "quảng trung", "âu cơ", "au co", "vincom",
            "dtu", "duy tan", "duy tân"
        };
        
        boolean hasLocationKeyword = false;
        for (String keyword : locationKeywords) {
            if (message.contains(keyword)) {
                hasLocationKeyword = true;
                break;
            }
        }
        
        // If has location keyword AND event-related words, prioritize location search
        if (hasLocationKeyword && message.matches(".*(sự kiện|event|có|gì|nào|hay|không).*")) {
            return Intent.SEARCH_BY_LOCATION;
        }
        
        // Also check for location prepositions
        if (message.matches(".*(ở|tại|địa điểm|location).*")) {
            return Intent.SEARCH_BY_LOCATION;
        }
        
        // Category search
        if (message.matches(".*(workshop|seminar|meetup|training|hội thảo|buổi học|sự kiện công nghệ|sự kiện văn hóa).*")) {
            return Intent.SEARCH_BY_CATEGORY;
        }
        
        // Time search
        if (message.matches(".*(hôm nay|tuần này|tháng này|ngày mai|sắp tới|gần đây|sớm nhất).*")) {
            return Intent.SEARCH_BY_TIME;
        }
        
        // Name search
        if (message.matches(".*(sự kiện.*tên|tìm.*tên|event.*name|.*show|.*festival|.*expo).*")) {
            return Intent.SEARCH_BY_NAME;
        }
        
        // Registration
        if (message.matches(".*(đăng ký|register|tham gia|join|how to register).*")) {
            return Intent.REGISTRATION_HELP;
        }
        
        // QR Check-in
        if (message.matches(".*(qr|check.in|checkin|mã qr|quét qr).*")) {
            return Intent.QR_CHECKIN_HELP;
        }
        
        // Event count
        if (message.matches(".*(có bao nhiêu|số lượng|count|tổng số|how many).*")) {
            return Intent.EVENT_COUNT;
        }
        
        // General search - check last (lowest priority)
        if (message.matches(".*(sự kiện|event|có sự kiện|sự kiện nào|danh sách).*")) {
            return Intent.GENERAL_SEARCH;
        }
        
        return Intent.UNKNOWN;
    }

    private ChatbotResponse handleGreeting() {
        return new ChatbotResponse(true, 
            "Xin chào! 👋 Tôi là trợ lý AI thông minh của EventQR.\n\n" +
            "Tôi có thể giúp bạn:\n" +
            "• 🔍 Tìm kiếm sự kiện theo địa điểm, danh mục, thời gian\n" +
            "• 📅 Xem danh sách sự kiện sắp diễn ra\n" +
            "• 🎫 Hướng dẫn đăng ký tham gia sự kiện\n" +
            "• 📱 Giải đáp về QR check-in\n" +
            "• 💡 Gợi ý sự kiện phù hợp với bạn\n\n" +
            "Bạn muốn tìm hiểu gì? 😊", null, null);
    }

    private ChatbotResponse searchEventsByLocation(String userMessage) {
        try {
            String lowerMessage = userMessage.toLowerCase();
            
            // Extended location keywords
            Map<String, List<String>> locationMap = new HashMap<>();
            locationMap.put("đà nẵng", Arrays.asList("đà nẵng", "da nang", "danang"));
            locationMap.put("hà nội", Arrays.asList("hà nội", "ha noi", "hanoi"));
            locationMap.put("hồ chí minh", Arrays.asList("hồ chí minh", "ho chi minh", "hcm", "sài gòn", "sai gon"));
            locationMap.put("quang trung", Arrays.asList("quang trung", "quảng trung"));
            locationMap.put("âu cơ", Arrays.asList("âu cơ", "au co"));
            locationMap.put("vincom", Arrays.asList("vincom"));
            locationMap.put("dtu", Arrays.asList("dtu", "duy tan", "duy tân"));
            
            String foundLocation = null;
            for (Map.Entry<String, List<String>> entry : locationMap.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (lowerMessage.contains(keyword)) {
                        foundLocation = entry.getKey();
                        break;
                    }
                }
                if (foundLocation != null) break;
            }
            
            List<Event> allEvents = eventRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            
            List<Event> upcomingEvents = allEvents.stream()
                .filter(e -> {
                    if (e.getStartTime() == null) return false;
                    if (e.getStatus() != null && e.getStatus().equalsIgnoreCase("CANCELLED")) return false;
                    return e.getStartTime().isAfter(now);
                })
                .collect(Collectors.toList());
            
            List<Event> filteredEvents = upcomingEvents;
            if (foundLocation != null) {
                final String location = foundLocation;
                filteredEvents = upcomingEvents.stream()
                    .filter(e -> {
                        if (e.getLocation() == null) return false;
                        String loc = e.getLocation().toLowerCase();
                        return locationMap.get(location).stream().anyMatch(loc::contains);
                    })
                    .collect(Collectors.toList());
            }
            
            filteredEvents = filteredEvents.stream()
                .sorted((e1, e2) -> {
                    if (e1.getStartTime() == null) return 1;
                    if (e2.getStartTime() == null) return -1;
                    return e1.getStartTime().compareTo(e2.getStartTime());
                })
                .limit(5)
                .collect(Collectors.toList());
            
            if (filteredEvents.isEmpty()) {
                String locationText = foundLocation != null ? " ở **" + foundLocation + "**" : "";
                
                // Check if there are any events at all (even past ones)
                long totalEventsAtLocation = 0;
                if (foundLocation != null) {
                    final String location = foundLocation;
                    totalEventsAtLocation = allEvents.stream()
                        .filter(e -> {
                            if (e.getLocation() == null) return false;
                            String loc = e.getLocation().toLowerCase();
                            return locationMap.get(location).stream().anyMatch(loc::contains);
                        })
                        .count();
                }
                
                StringBuilder response = new StringBuilder();
                response.append("Hiện tại không có sự kiện sắp diễn ra").append(locationText).append(".\n\n");
                
                if (totalEventsAtLocation > 0) {
                    response.append("💡 Tuy nhiên, đã có ").append(totalEventsAtLocation)
                           .append(" sự kiện từng diễn ra tại đây.\n\n");
                }
                
                response.append("💡 **Gợi ý:**\n")
                       .append("• Xem tất cả sự kiện sắp diễn ra trên trang chủ\n")
                       .append("• Thử tìm kiếm theo danh mục khác\n")
                       .append("• Đăng ký nhận thông báo khi có sự kiện mới\n")
                       .append("• Tìm sự kiện ở địa điểm khác\n\n")
                       .append("Bạn muốn tôi tìm sự kiện ở đâu khác không? 📅");
                
                return new ChatbotResponse(true, response.toString(), null, null);
            }
            
            StringBuilder response = new StringBuilder();
            if (foundLocation != null) {
                response.append(String.format("🎯 Tìm thấy %d sự kiện sắp diễn ra ở **%s**:\n\n", 
                    filteredEvents.size(), foundLocation));
            } else {
                response.append(String.format("📅 Có %d sự kiện sắp diễn ra:\n\n", filteredEvents.size()));
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (int i = 0; i < filteredEvents.size(); i++) {
                Event event = filteredEvents.get(i);
                response.append(String.format("**%d. %s**\n", i + 1, event.getTitle()));
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    String desc = event.getDescription().length() > 100 
                        ? event.getDescription().substring(0, 100) + "..." 
                        : event.getDescription();
                    response.append("   ").append(desc).append("\n");
                }
                response.append(String.format("   📍 %s\n", event.getLocation()));
                if (event.getStartTime() != null) {
                    response.append(String.format("   📅 %s\n", event.getStartTime().format(formatter)));
                }
                if (event.getCategory() != null) {
                    response.append(String.format("   🏷️ %s\n", event.getCategory()));
                }
                if (event.getMaxParticipants() != null) {
                    response.append(String.format("   👥 Tối đa %d người\n", event.getMaxParticipants()));
                }
                response.append("\n");
            }
            
            response.append("💡 Bạn muốn đăng ký sự kiện nào không? Tôi có thể hướng dẫn bạn! 🎫");
            
            return new ChatbotResponse(true, response.toString(), null, null);
            
        } catch (Exception e) {
            logger.error("❌ Lỗi khi tìm kiếm sự kiện theo tên: {}", e.getMessage(), e);
            return new ChatbotResponse(true,
                "Xin lỗi, tôi không thể tìm kiếm sự kiện lúc này. " +
                "Vui lòng truy cập trang chủ để xem danh sách sự kiện. 📅", null, null);
        }
    }

    private ChatbotResponse searchEventsByCategory(String userMessage) {
        try {
            String lowerMessage = userMessage.toLowerCase();
            
            Map<String, List<String>> categoryMap = new HashMap<>();
            categoryMap.put("workshop", Arrays.asList("workshop", "buổi học", "khóa học"));
            categoryMap.put("seminar", Arrays.asList("seminar", "hội thảo", "hội nghị"));
            categoryMap.put("meetup", Arrays.asList("meetup", "gặp gỡ", "giao lưu"));
            categoryMap.put("training", Arrays.asList("training", "đào tạo", "huấn luyện"));
            
            final String[] foundCategoryArray = {null};
            for (Map.Entry<String, List<String>> entry : categoryMap.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (lowerMessage.contains(keyword)) {
                        foundCategoryArray[0] = entry.getKey();
                        break;
                    }
                }
                if (foundCategoryArray[0] != null) break;
            }
            final String foundCategory = foundCategoryArray[0];
            
            List<Event> allEvents = eventRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            
            List<Event> filteredEvents = allEvents.stream()
                .filter(e -> {
                    if (e.getStartTime() == null) return false;
                    if (e.getStatus() != null && e.getStatus().equalsIgnoreCase("CANCELLED")) return false;
                    if (!e.getStartTime().isAfter(now)) return false;
                    if (foundCategory != null && e.getCategory() != null) {
                        return e.getCategory().toLowerCase().equals(foundCategory);
                    }
                    return true;
                })
                .sorted((e1, e2) -> {
                    if (e1.getStartTime() == null) return 1;
                    if (e2.getStartTime() == null) return -1;
                    return e1.getStartTime().compareTo(e2.getStartTime());
                })
                .limit(5)
                .collect(Collectors.toList());
            
            if (filteredEvents.isEmpty()) {
                String categoryText = foundCategory != null ? " thuộc danh mục " + foundCategory : "";
                return new ChatbotResponse(true,
                    "Hiện tại không có sự kiện sắp diễn ra" + categoryText + ".\n\n" +
                    "Bạn có muốn xem các danh mục khác không? 📅", null, null);
            }
            
            StringBuilder response = new StringBuilder();
            if (foundCategory != null) {
                response.append(String.format("🎯 Tìm thấy %d sự kiện **%s** sắp diễn ra:\n\n", 
                    filteredEvents.size(), foundCategory));
            } else {
                response.append(String.format("📅 Có %d sự kiện sắp diễn ra:\n\n", filteredEvents.size()));
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (int i = 0; i < filteredEvents.size(); i++) {
                Event event = filteredEvents.get(i);
                response.append(String.format("**%d. %s**\n", i + 1, event.getTitle()));
                response.append(String.format("   📍 %s | 📅 %s\n", 
                    event.getLocation(),
                    event.getStartTime() != null ? event.getStartTime().format(formatter) : "Chưa xác định"));
                response.append("\n");
            }
            
            return new ChatbotResponse(true, response.toString(), null, null);
            
        } catch (Exception e) {
            logger.error("❌ Lỗi khi tìm kiếm sự kiện theo danh mục: {}", e.getMessage(), e);
            return new ChatbotResponse(true,
                "Xin lỗi, tôi không thể tìm kiếm sự kiện lúc này.", null, null);
        }
    }

    private ChatbotResponse searchEventsByTime(String userMessage) {
        try {
            String lowerMessage = userMessage.toLowerCase();
            LocalDateTime now = LocalDateTime.now();
            final LocalDateTime[] filterTimeArray = {now};
            
            if (lowerMessage.contains("hôm nay") || lowerMessage.contains("today")) {
                filterTimeArray[0] = now.plusDays(1);
            } else if (lowerMessage.contains("tuần này") || lowerMessage.contains("this week")) {
                filterTimeArray[0] = now.plusWeeks(1);
            } else if (lowerMessage.contains("tháng này") || lowerMessage.contains("this month")) {
                filterTimeArray[0] = now.plusMonths(1);
            } else if (lowerMessage.contains("ngày mai") || lowerMessage.contains("tomorrow")) {
                filterTimeArray[0] = now.plusDays(2);
            } else if (lowerMessage.contains("sớm nhất") || lowerMessage.contains("soonest")) {
                filterTimeArray[0] = now.plusDays(7);
            }
            final LocalDateTime filterTime = filterTimeArray[0];
            
            List<Event> filteredEvents = eventRepository.findAll().stream()
                .filter(e -> {
                    if (e.getStartTime() == null) return false;
                    if (e.getStatus() != null && e.getStatus().equalsIgnoreCase("CANCELLED")) return false;
                    return e.getStartTime().isAfter(now) && e.getStartTime().isBefore(filterTime);
                })
                .sorted((e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime()))
                .limit(5)
                .collect(Collectors.toList());
            
            if (filteredEvents.isEmpty()) {
                return new ChatbotResponse(true,
                    "Không có sự kiện nào trong khoảng thời gian này.\n\n" +
                    "Bạn có muốn xem tất cả sự kiện sắp diễn ra không? 📅", null, null);
            }
            
            StringBuilder response = new StringBuilder();
            response.append(String.format("📅 Tìm thấy %d sự kiện:\n\n", filteredEvents.size()));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (int i = 0; i < filteredEvents.size(); i++) {
                Event event = filteredEvents.get(i);
                response.append(String.format("**%d. %s**\n", i + 1, event.getTitle()));
                response.append(String.format("   📍 %s | 📅 %s\n", 
                    event.getLocation(),
                    event.getStartTime().format(formatter)));
                response.append("\n");
            }
            
            return new ChatbotResponse(true, response.toString(), null, null);
            
        } catch (Exception e) {
            logger.error("❌ Lỗi khi tìm kiếm sự kiện theo danh mục: {}", e.getMessage(), e);
            return new ChatbotResponse(true,
                "Xin lỗi, tôi không thể tìm kiếm sự kiện lúc này.", null, null);
        }
    }

    private ChatbotResponse searchEventsByName(String userMessage) {
        try {
            List<Event> allEvents = eventRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            
            // Extract potential event name keywords
            String[] words = userMessage.toLowerCase().split("\\s+");
            List<String> keywords = Arrays.stream(words)
                .filter(w -> w.length() > 3)
                .collect(Collectors.toList());
            
            List<Event> matchedEvents = allEvents.stream()
                .filter(e -> {
                    if (e.getStartTime() == null) return false;
                    if (e.getStatus() != null && e.getStatus().equalsIgnoreCase("CANCELLED")) return false;
                    if (!e.getStartTime().isAfter(now)) return false;
                    
                    String title = e.getTitle() != null ? e.getTitle().toLowerCase() : "";
                    String desc = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
                    
                    return keywords.stream().anyMatch(k -> title.contains(k) || desc.contains(k));
                })
                .sorted((e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime()))
                .limit(5)
                .collect(Collectors.toList());
            
            if (matchedEvents.isEmpty()) {
                return new ChatbotResponse(true,
                    "Không tìm thấy sự kiện phù hợp với từ khóa của bạn.\n\n" +
                    "Bạn có thể thử:\n" +
                    "• Tìm kiếm theo địa điểm\n" +
                    "• Tìm kiếm theo danh mục\n" +
                    "• Xem tất cả sự kiện sắp diễn ra\n\n" +
                    "Cần hỗ trợ gì khác? 🔍", null, null);
            }
            
            StringBuilder response = new StringBuilder();
            response.append(String.format("🔍 Tìm thấy %d sự kiện phù hợp:\n\n", matchedEvents.size()));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (int i = 0; i < matchedEvents.size(); i++) {
                Event event = matchedEvents.get(i);
                response.append(String.format("**%d. %s**\n", i + 1, event.getTitle()));
                if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                    response.append("   ").append(event.getDescription().substring(0, Math.min(80, event.getDescription().length()))).append("\n");
                }
                response.append(String.format("   📍 %s | 📅 %s\n", 
                    event.getLocation(),
                    event.getStartTime().format(formatter)));
                response.append("\n");
            }
            
            return new ChatbotResponse(true, response.toString(), null, null);
            
        } catch (Exception e) {
            logger.error("❌ Lỗi khi tìm kiếm sự kiện theo danh mục: {}", e.getMessage(), e);
            return new ChatbotResponse(true,
                "Xin lỗi, tôi không thể tìm kiếm sự kiện lúc này.", null, null);
        }
    }

    private ChatbotResponse handleRegistrationHelp() {
        return new ChatbotResponse(true,
            "📝 **Hướng dẫn đăng ký tham gia sự kiện:**\n\n" +
            "1️⃣ **Đăng nhập** vào tài khoản của bạn\n" +
            "2️⃣ **Tìm sự kiện** bạn muốn tham gia\n" +
            "3️⃣ **Nhấn nút 'Đăng Ký'** trên trang chi tiết sự kiện\n" +
            "4️⃣ **Nhận vé QR code** qua email tự động\n" +
            "5️⃣ **Lưu vé** để quét QR khi check-in tại sự kiện\n\n" +
            "💡 **Lưu ý:**\n" +
            "• Mỗi sự kiện có số lượng giới hạn người tham gia\n" +
            "• Đăng ký sớm để đảm bảo có chỗ\n" +
            "• Kiểm tra email để nhận vé QR code\n\n" +
            "Bạn cần hỗ trợ thêm gì không? 🎫", null, null);
    }

    private ChatbotResponse handleQRCheckinHelp() {
        return new ChatbotResponse(true,
            "📱 **Về QR Check-in:**\n\n" +
            "**Cách 1: Quét QR Code**\n" +
            "• Mở camera điện thoại\n" +
            "• Quét mã QR trên vé\n" +
            "• Hệ thống tự động check-in\n\n" +
            "**Cách 2: Nhập mã thủ công**\n" +
            "• Vào trang QR Check-in\n" +
            "• Nhập mã QR từ vé\n" +
            "• Nhấn 'Check-in'\n\n" +
            "💡 **Lưu ý:**\n" +
            "• Mỗi vé chỉ check-in được 1 lần\n" +
            "• Mã QR là duy nhất cho mỗi vé\n" +
            "• Check-in tại địa điểm sự kiện\n\n" +
            "Có vấn đề gì với QR code không? 📱", null, null);
    }

    private ChatbotResponse handleEventCount() {
        try {
            List<Event> allEvents = eventRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            
            long upcomingCount = allEvents.stream()
                .filter(e -> {
                    if (e.getStartTime() == null) return false;
                    if (e.getStatus() != null && e.getStatus().equalsIgnoreCase("CANCELLED")) return false;
                    return e.getStartTime().isAfter(now);
                })
                .count();
            
            long totalCount = allEvents.size();
            
            return new ChatbotResponse(true,
                String.format("📊 **Thống kê sự kiện:**\n\n" +
                    "• Tổng số sự kiện: **%d**\n" +
                    "• Sự kiện sắp diễn ra: **%d**\n" +
                    "• Sự kiện đã kết thúc: **%d**\n\n" +
                    "Bạn muốn xem danh sách sự kiện sắp diễn ra không? 📅",
                    totalCount, upcomingCount, totalCount - upcomingCount), null, null);
        } catch (Exception e) {
            return new ChatbotResponse(true,
                "Xin lỗi, tôi không thể lấy thống kê lúc này.", null, null);
        }
    }

    private ChatbotResponse searchUpcomingEvents(String userMessage) {
        try {
            List<Event> allEvents = eventRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            
            List<Event> upcomingEvents = allEvents.stream()
                .filter(e -> {
                    if (e.getStartTime() == null) return false;
                    if (e.getStatus() != null && e.getStatus().equalsIgnoreCase("CANCELLED")) return false;
                    return e.getStartTime().isAfter(now);
                })
                .sorted((e1, e2) -> {
                    if (e1.getStartTime() == null) return 1;
                    if (e2.getStartTime() == null) return -1;
                    return e1.getStartTime().compareTo(e2.getStartTime());
                })
                .limit(5)
                .collect(Collectors.toList());
            
            if (upcomingEvents.isEmpty()) {
                return new ChatbotResponse(true,
                    "Hiện tại không có sự kiện sắp diễn ra.\n\n" +
                    "💡 Bạn có thể:\n" +
                    "• Xem tất cả sự kiện trên trang chủ\n" +
                    "• Đăng ký nhận thông báo khi có sự kiện mới\n" +
                    "• Tìm kiếm theo địa điểm hoặc danh mục\n\n" +
                    "Cần hỗ trợ gì khác không? 📅", null, null);
            }
            
            StringBuilder response = new StringBuilder();
            response.append(String.format("📅 **Có %d sự kiện sắp diễn ra:**\n\n", upcomingEvents.size()));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (int i = 0; i < upcomingEvents.size(); i++) {
                Event event = upcomingEvents.get(i);
                response.append(String.format("**%d. %s**\n", i + 1, event.getTitle()));
                response.append(String.format("   📍 %s\n", event.getLocation()));
                if (event.getStartTime() != null) {
                    response.append(String.format("   📅 %s\n", event.getStartTime().format(formatter)));
                }
                if (event.getCategory() != null) {
                    response.append(String.format("   🏷️ %s\n", event.getCategory()));
                }
                response.append("\n");
            }
            
            response.append("💡 Bạn muốn xem chi tiết hoặc đăng ký sự kiện nào không? 🎫");
            
            return new ChatbotResponse(true, response.toString(), null, null);
            
        } catch (Exception e) {
            logger.error("❌ Lỗi khi lấy danh sách sự kiện: {}", e.getMessage(), e);
            return new ChatbotResponse(true,
                "Xin lỗi, tôi không thể lấy danh sách sự kiện lúc này. " +
                "Vui lòng truy cập trang chủ để xem. 📅", null, null);
        }
    }

    private ChatbotResponse handleGeneralQuery(String userMessage) {
        // Try to extract any useful information and provide helpful response
        return new ChatbotResponse(true,
            "Cảm ơn bạn đã liên hệ! 💬\n\n" +
            "Tôi có thể giúp bạn:\n" +
            "• 🔍 Tìm kiếm sự kiện (theo địa điểm, danh mục, thời gian)\n" +
            "• 📅 Xem danh sách sự kiện sắp diễn ra\n" +
            "• 🎫 Hướng dẫn đăng ký tham gia\n" +
            "• 📱 Giải đáp về QR check-in\n" +
            "• 📊 Thống kê sự kiện\n\n" +
            "Bạn có thể hỏi tôi:\n" +
            "• \"Có sự kiện nào ở Đà Nẵng không?\"\n" +
            "• \"Sự kiện workshop sắp tới\"\n" +
            "• \"Hướng dẫn đăng ký\"\n" +
            "• \"Có bao nhiêu sự kiện?\"\n\n" +
            "Bạn muốn biết gì? 😊", null, null);
    }
}
