package com.eventhive.backend.repository;

import com.eventhive.backend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    // All seat of event included
    List<Seat> findByEventId(Long eventId);
}