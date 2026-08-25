package com.eventhive.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ටිකට් එක මිලදී ගන්නා User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // අදාළ Event එක
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // මිලදී ගත් පුටු ලැයිස්තුව (එක්කෙනෙක්ට පුටු කිහිපයක් ගන්න පුළුවන්)
    @OneToMany
    @JoinColumn(name = "booking_id")
    private List<Seat> seats;

    // මුළු මුදල
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    // උදා: CONFIRMED, CANCELLED, PENDING
    @Column(nullable = false)
    private String status;
}