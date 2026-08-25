package com.eventhive.backend.controller;

import com.eventhive.backend.ai.AIChatService;
import com.eventhive.backend.ai.EventEnrichmentService;
import com.eventhive.backend.ai.EventRecommendationService;
import com.eventhive.backend.ai.VenueImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private EventEnrichmentService eventEnrichmentService;

    @Autowired
    private AIChatService aiChatService;

    @Autowired
    private EventRecommendationService eventRecommendationService;

    @Autowired
    private VenueImageService venueImageService;

    // Feature 3: Auto-description & tagging
    @PostMapping("/enrich-event")
    public ResponseEntity<Map<String, String>> enrichEvent(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String category = request.get("category");
        String venue = request.get("venue");
        String aiResponse = eventEnrichmentService.generateDescriptionAndTags(title, category, venue);
        return ResponseEntity.ok(Map.of("result", aiResponse));
    }

    // Feature 1: AI Chatbot
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithAI(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String aiResponse = aiChatService.getChatbotResponse(message);
        return ResponseEntity.ok(Map.of("reply", aiResponse));
    }

    // Feature 2: Smart Recommendations
    @PostMapping("/recommend")
    public ResponseEntity<Map<String, String>> recommendEvents(@RequestBody Map<String, String> request) {
        String preferences = request.get("preferences");
        String aiResponse = eventRecommendationService.getRecommendations(preferences);
        return ResponseEntity.ok(Map.of("recommendations", aiResponse));
    }

    // Feature 4: Generate Venue Layout Image
    @PostMapping(value = "/generate-venue", produces = org.springframework.http.MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> generateVenueImage(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        byte[] imageBytes = venueImageService.generateImage(prompt);

        if (imageBytes != null) {
            return ResponseEntity.ok().body(imageBytes);
        } else {
            return ResponseEntity.status(500).build();
        }
    }
}