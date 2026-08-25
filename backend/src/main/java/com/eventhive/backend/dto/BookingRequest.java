package com.eventhive.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookingRequest {
    private Long eventId;
    private List<Long> seatIds; // සිලෙක්ට් කරපු පුටු වල ID ලැයිස්තුව (උදා: [6, 7, 8])
}