package com.eventhive.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class SeatCustomSaveRequest {
    private Long eventId;
    private List<CustomSeatDTO> seats;
}
