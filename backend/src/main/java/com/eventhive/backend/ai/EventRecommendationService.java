package com.eventhive.backend.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventRecommendationService {

    @Autowired
    private AIClientService aiClientService;

    public String getRecommendations(String preferences) {

        // AI එකට උපදෙස් දෙන System Prompt එක (මෙතනත් අපි Markdown එපා කියලම දෙනවා)
        String systemPrompt = "You are an expert event recommender for the 'EventHive' platform. " +
                "Based on the user's preferences, suggest 3 exciting event ideas they could attend or organize. " +
                "Provide the event title and a short reason why it matches their preferences. " +
                "CRITICAL: Do NOT use any Markdown formatting like asterisks, tables, or hashes. Respond in simple, conversational text.";

        // User ගේ කැමැත්ත AI මොළයට යවනවා
        String userPrompt = "User Preferences: " + preferences;

        return aiClientService.callGroqAPI(systemPrompt, userPrompt);
    }
}