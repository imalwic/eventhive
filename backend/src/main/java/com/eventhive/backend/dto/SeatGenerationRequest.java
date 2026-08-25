package com.eventhive.backend.dto;

import lombok.Data;

@Data
public class SeatGenerationRequest {
    private Long eventId;
    private Long ticketCategoryId;
    private int numberOfRows;   // පේළි කීයක් ඕනද? (උදා: 5)
    private int seatsPerRow;    // එක පේළියකට පුටු කීයක් ඕනද? (උදා: 10)

    // UI එකේ අඳින්න ඕන කරන දත්ත
    private double startX;      // Grid එකේ පටන් ගන්න X තැන
    private double startY;      // Grid එකේ පටන් ගන්න Y තැන
    private double seatGap;     // පුටු දෙකක් අතර පරතරය (X අක්ෂයේ)
    private double rowGap;      // පේළි දෙකක් අතර පරතරය (Y අක්ෂයේ)
}