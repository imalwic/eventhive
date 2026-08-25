package com.eventhive.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(nullable = false)
    private String packageName; // FREE, BASIC, STANDARD, PRO

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer maxEvents; // -1 for unlimited

    @Column(nullable = false)
    private Integer eventsUsed = 0;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String status; // PENDING, ACTIVE, EXPIRED, REJECTED

    @Column(name = "payment_method")
    private String paymentMethod; // CARD, BANK_TRANSFER

    @Column(name = "payment_proof")
    private String paymentProof; // Image URL if Bank Transfer

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
