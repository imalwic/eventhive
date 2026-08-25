package com.eventhive.backend.controller;

import com.eventhive.backend.entity.SiteSettings;
import com.eventhive.backend.service.SiteSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SiteSettingsController {

    @Autowired
    private SiteSettingsService service;

    @GetMapping
    public ResponseEntity<SiteSettings> getSettings() {
        return ResponseEntity.ok(service.getSettings());
    }

    @PutMapping
    public ResponseEntity<SiteSettings> updateSettings(@RequestBody SiteSettings settings) {
        // Ideally verify authentication role is ADMIN here
        return ResponseEntity.ok(service.updateSettings(settings));
    }
}
