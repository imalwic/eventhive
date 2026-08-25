package com.eventhive.backend.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventEnrichmentService {

    @Autowired
    private AIClientService aiClientService;

    public String generateDescriptionAndTags(String title, String category, String venue) {

        // AI එක හැසිරෙන්න ඕන විදිය (System Prompt)
        String systemPrompt = "You are a professional event copywriter. Generate an exciting, professional event description (around 60-80 words) and suggest 3 to 5 relevant tags. " +
                "Strictly return the response in this exact format without any extra text, markdown, or <think> blocks:\n" +
                "Description: [Your description here]\n" +
                "Tags: [tag1, tag2, tag3]";

        // අපි AI එකට යවන අපේ ඉවෙන්ට් එකේ විස්තර ටික (User Prompt)
        String userPrompt = String.format("Event Title: %s\nCategory: %s\nVenue: %s", title, category, venue);

        // අර කලින් හදපු AIClientService එක හරහා Groq API එකට කතා කරනවා
        String response = aiClientService.callGroqAPI(systemPrompt, userPrompt);
        
        // Remove <think> blocks if any
        response = response.replaceAll("(?s)<think>.*?</think>", "").trim();
        // Remove markdown bolding
        response = response.replace("**", "");
        
        return response;
    }
}