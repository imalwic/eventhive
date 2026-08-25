package com.eventhive.backend.dto;

import lombok.Data;

@Data
public class SeatStatsDTO {
    private int standaloneChairs;
    private int tableChairs;
    private int standingZones;
    private int tables;
}
