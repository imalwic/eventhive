package com.eventhive.backend.controller;

import com.eventhive.backend.dto.SeatGenerationRequest;
import com.eventhive.backend.entity.Seat;
import com.eventhive.backend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    // ඔටෝමැටික් පුටු (X,Y ඛණ්ඩාංක එක්ක) ජෙනරේට් කරන API එක
    @PostMapping("/generate")
    public ResponseEntity<List<Seat>> generateSeats(@RequestBody SeatGenerationRequest request) {
        List<Seat> generatedSeats = seatService.generateDynamicSeats(request);
        return ResponseEntity.ok(generatedSeats);
    }

    // Get all seats for an event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Seat>> getSeatsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatService.getSeatsByEvent(eventId));
    }

    // Save custom visual layout
    @PostMapping("/save-custom")
    public ResponseEntity<List<Seat>> saveCustomSeats(@RequestBody com.eventhive.backend.dto.SeatCustomSaveRequest request) {
        List<Seat> savedSeats = seatService.saveCustomSeats(request);
        return ResponseEntity.ok(savedSeats);
    }

    // Get statistics about the seats for an event
    @GetMapping("/event/{eventId}/stats")
    public ResponseEntity<com.eventhive.backend.dto.SeatStatsDTO> getSeatStats(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatService.getSeatStats(eventId));
    }
}