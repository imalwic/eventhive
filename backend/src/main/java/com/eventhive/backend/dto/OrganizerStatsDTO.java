package com.eventhive.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizerStatsDTO {
    private long totalEvents;
    private long totalTicketsSold;
    private double totalRevenue;
}
