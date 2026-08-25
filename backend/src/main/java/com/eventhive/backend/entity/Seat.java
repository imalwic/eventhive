package com.eventhive.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "seats")
@Data
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // VVIP, VIP, ODC, BOX වගේ ටිකට් කාණ්ඩය අඳුරගන්න
    @Column(name = "tier_name")
    private String tierName;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    // මේක Chair එකක්ද, Table එකක්ද, Standing Zone එකක්ද කියලා
    @Column(name = "seat_type")
    private String seatType;

    // Frontend UI එකේ Seat එක පෙන්විය යුතු තැන (X අක්ෂය)
    @Column(name = "x_coordinate")
    private Double xCoordinate;

    // Frontend UI එකේ Seat එක පෙන්විය යුතු තැන (Y අක්ෂය)
    @Column(name = "y_coordinate")
    private Double yCoordinate;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String status; // Example: AVAILABLE, LOCKED, BOOKED

    // Optimistic Locking for used @Version annotation
    @Version
    private Integer version;
}