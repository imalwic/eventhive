package com.eventhive.backend.dto;

import lombok.Data;

@Data
public class CustomSeatDTO {
    private String type; // CHAIR, TABLE, STANDING
    private Double x;
    private Double y;
    private Long categoryId;
}
