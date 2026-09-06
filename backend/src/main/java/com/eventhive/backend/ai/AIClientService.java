package com.eventhive.backend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIClientService {

    // application.properties එකේ තියෙන Key එක මෙතනට ගන්නවා
    @Value("${groq.api.key}")
    private String apiKey;

    // Groq API Endpoint එක
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    // HTTP Requests යවන්න පාවිච්චි කරන්නේ RestTemplate
    private final RestTemplate restTemplate = new RestTemplate();

    public String callGroqAPI(String systemPrompt, String userPrompt) {

        // 1. Headers හදනවා (API Key එකත් එක්ක)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 2. System Prompt එක (AI එක හැසිරෙන්න ඕන විදිය)
        Map<String, Object> messageSystem = new HashMap<>();
        messageSystem.put("role", "system");
        messageSystem.put("content", systemPrompt);

        // 3. User Prompt එක (අපි අහන ප්‍රශ්නය)
        Map<String, Object> messageUser = new HashMap<>();
        messageUser.put("role", "user");
        messageUser.put("content", userPrompt);

        // 4. Request Body එක හදනවා (Model එක විදියට Mixtral පාවිච්චි කරනවා)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "mixtral-8x7b-32768");
        requestBody.put("messages", List.of(messageSystem, messageUser));
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            // Groq API එකට Request එක යවනවා
            ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_API_URL, request, Map.class);

            // Response එකෙන් AI එක දීපු උත්තරේ විතරක් වෙන් කරලා ගන්නවා
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            System.out.println("Groq API Error: " + e.getMessage());
            return "AI Service is temporarily unavailable.";
        }
    }
}