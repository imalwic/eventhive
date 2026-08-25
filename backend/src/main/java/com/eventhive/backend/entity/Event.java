package com.eventhive.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Database into organizer_id create Foreign Key
    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ai_generated_desc", columnDefinition = "TEXT")
    private String aiGeneratedDesc;

    // AI එකෙන් ජෙනරේට් කරන Venue Layout පින්තූරයේ URL එක හෝ Path එක
    @Column(name = "venue_image_url", columnDefinition = "TEXT")
    private String venueImageUrl;

    @Column(nullable = false)
    private String venue;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    private String category;

    // Jackson (JSON) වලට "isTicketed" කියන නමම පාවිච්චි කරන්න කියලා මෙතනින් කියනවා
    @JsonProperty("isTicketed")
    @Column(name = "is_ticketed", nullable = false, columnDefinition = "boolean default false")
    private boolean isTicketed;

    // Tags are many there for given that List
    @ElementCollection
    private List<String> tags;

    private String status; // Example: PENDING, APPROVED

    // ඉවෙන්ට් එකට අදාළව Organizer හදන ටිකට් කාණ්ඩ ලැයිස්තුව
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketCategory> ticketCategories;
}