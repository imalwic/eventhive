package com.eventhive.backend.controller;

import com.eventhive.backend.entity.ContactMessage;
import com.eventhive.backend.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact/messages")
public class ContactMessageController {

    @Autowired
    private ContactMessageRepository repository;

    @PostMapping
    public ResponseEntity<ContactMessage> submitMessage(@RequestBody ContactMessage message) {
        return ResponseEntity.ok(repository.save(message));
    }

    @GetMapping
    public ResponseEntity<List<ContactMessage>> getAllMessages() {
        // Ideally verify authentication role is ADMIN here
        return ResponseEntity.ok(repository.findAllByOrderByCreatedAtDesc());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ContactMessage> markAsRead(@PathVariable Long id) {
        // Ideally verify authentication role is ADMIN here
        return repository.findById(id).map(msg -> {
            msg.setStatus("READ");
            return ResponseEntity.ok(repository.save(msg));
        }).orElse(ResponseEntity.notFound().build());
    }
}
