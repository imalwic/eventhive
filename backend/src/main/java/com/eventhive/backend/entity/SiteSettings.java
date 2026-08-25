package com.eventhive.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class SiteSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contactPhone;
    private String contactEmail;
    private String contactAddress;

    @Column(columnDefinition = "TEXT")
    private String privacyPolicyContent;

    @Column(columnDefinition = "TEXT")
    private String termsOfServiceContent;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
}
