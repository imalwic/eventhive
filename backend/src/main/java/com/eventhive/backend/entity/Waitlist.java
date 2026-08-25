package com.eventhive.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "waitlist")
@Data
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // queue place
    @Column(nullable = false)
    private Integer position;

    // Seat is free  notify message
    @Column(nullable = false)
    private Boolean notified = false;
}