package com.eventhive.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_preferences")
@Data
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String category; // Example: "Technology", "Music"

    @Column(name = "interaction_score", nullable = false)
    private Integer interactionScore = 0; // AI to given the score
}