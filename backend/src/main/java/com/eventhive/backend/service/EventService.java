package com.eventhive.backend.service;

import com.eventhive.backend.ai.EventEnrichmentService;
import com.eventhive.backend.entity.Event;
import com.eventhive.backend.entity.User;
import com.eventhive.backend.repository.EventRepository;
import com.eventhive.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // අපේ AI Service එක මෙතනට ගේනවා
    @Autowired
    private EventEnrichmentService eventEnrichmentService;

    public Event createEvent(Event event, String organizerEmail) {

        // 1. Event එක හදන කෙනාව (Organizer) හොයාගන්නවා
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        event.setOrganizer(organizer);

        // 2. AI Magic එක! 🌟 (Description එක හිස් නම් විතරක් AI එකෙන් ලියවනවා)
        if (event.getDescription() == null || event.getDescription().isEmpty()) {
            String aiDesc = eventEnrichmentService.generateDescriptionAndTags(
                    event.getTitle(),
                    event.getCategory(),
                    event.getVenue()
            );
            // ඒ ජෙනරේට් වුණු විස්තරය Database එකේ සේව් වෙන්න සෙට් කරනවා
            event.setAiGeneratedDesc(aiDesc);
        }

        // Status සහ Date එක ඔටෝ සෙට් කිරීම
        event.setStatus("PENDING");
        if(event.getEventDate() == null) {
            event.setEventDate(LocalDateTime.now().plusDays(30)); // නිකන් default date එකක්
        }

        // 3. Database එකේ සේව් කරනවා
        return eventRepository.save(event);
    }
}