package com.eventhive.backend.controller;

import com.eventhive.backend.entity.Waitlist;
import com.eventhive.backend.service.WaitlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {

    @Autowired
    private WaitlistService waitlistService;

    // පෝලිමට එකතු වෙන්න කතා කරන API එක
    @PostMapping("/join/{eventId}")
    public ResponseEntity<?> joinWaitlist(@PathVariable Long eventId) {
        try {
            // JWT එකෙන් ලොග් වෙලා ඉන්න කෙනාව අඳුරගන්නවා
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // සර්විස් එකට යවලා පෝලිමට දානවා
            Waitlist waitlistEntry = waitlistService.joinWaitlist(eventId, userEmail);
            return ResponseEntity.ok(waitlistEntry);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyWaitlists() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            return ResponseEntity.ok(waitlistService.getUserWaitlists(userEmail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}