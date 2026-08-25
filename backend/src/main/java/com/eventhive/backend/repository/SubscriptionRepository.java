package com.eventhive.backend.repository;

import com.eventhive.backend.entity.Subscription;
import com.eventhive.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    // Find all subscriptions for a specific organizer
    List<Subscription> findByOrganizerOrderByCreatedAtDesc(User organizer);

    // Find the currently active subscription for an organizer
    @Query("SELECT s FROM Subscription s WHERE s.organizer = :organizer AND s.status = 'ACTIVE' AND s.endDate > CURRENT_TIMESTAMP AND (s.maxEvents = -1 OR s.eventsUsed < s.maxEvents) ORDER BY s.createdAt DESC LIMIT 1")
    Optional<Subscription> findActiveSubscriptionForOrganizer(@org.springframework.data.repository.query.Param("organizer") User organizer);

    // Find all subscriptions by status
    List<Subscription> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find subscriptions by multiple statuses
    List<Subscription> findByStatusIn(List<String> statuses);

    // Calculate total revenue from ACTIVE and EXPIRED subscriptions
    @Query("SELECT SUM(s.price) FROM Subscription s WHERE s.status IN ('ACTIVE', 'EXPIRED')")
    Double sumTotalRevenue();
}
