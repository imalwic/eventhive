package com.eventhive.backend.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIChatService {

    @Autowired
    private AIClientService aiClientService;

    public String getChatbotResponse(String userMessage) {

        // System Prompt එකට අපි අලුත් නීතියක් දැම්මා (No Markdown)
        String systemPrompt = "You are a friendly, helpful, and energetic AI assistant for an event management platform named 'EventHive'. " +
                "Your job is to assist users with their event-related queries, guide them on how to use the platform, and provide event suggestions. " +
                "Keep your answers concise, engaging, and directly related to events. " +
                "CRITICAL: Do NOT use any Markdown formatting. Do not use asterisks (**), tables (|), hashes (#), or bullet points. Respond in simple, plain conversational text only.";

        return aiClientService.callGroqAPI(systemPrompt, userMessage);
    }
}