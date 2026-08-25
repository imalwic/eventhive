package com.eventhive.backend.controller;

import com.eventhive.backend.entity.Ticket;
import com.eventhive.backend.entity.User;
import com.eventhive.backend.repository.TicketRepository;
import com.eventhive.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // Check-in endpoint for Organizer QR Scanner
    @PostMapping("/{uuid}/checkin")
    public ResponseEntity<?> checkInTicket(@PathVariable String uuid, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String organizerEmail = authentication.getName();
        Optional<User> organizerOpt = userRepository.findByEmail(organizerEmail);
        
        if (organizerOpt.isEmpty() || !organizerOpt.get().getRole().equals("ORGANIZER")) {
            return ResponseEntity.status(403).body("Only organizers can check-in tickets");
        }

        Optional<Ticket> ticketOpt = ticketRepository.findByUuid(uuid);
        
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("status", "INVALID", "message", "Invalid Ticket QR Code"));
        }
        
        Ticket ticket = ticketOpt.get();
        
        // Verify this organizer actually owns this event
        if (!ticket.getEvent().getOrganizer().getId().equals(organizerOpt.get().getId())) {
            return ResponseEntity.status(403).body(Map.of("status", "INVALID", "message", "This ticket belongs to another event"));
        }
        
        if (ticket.getIsCheckedIn()) {
            return ResponseEntity.ok(Map.of(
                    "status", "ALREADY_CHECKED_IN",
                    "message", "Ticket was already scanned at " + ticket.getCheckedInAt(),
                    "attendeeName", ticket.getBooking().getUser().getName(),
                    "ticketCategory", ticket.getTicketCategory().getName()
            ));
        }
        
        // Check-in successful
        ticket.setIsCheckedIn(true);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Check-in successful",
                "attendeeName", ticket.getBooking().getUser().getName(),
                "ticketCategory", ticket.getTicketCategory().getName(),
                "isGroupTicket", ticket.getIsGroupTicket()
        ));
    }
}
