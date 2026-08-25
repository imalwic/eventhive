package com.eventhive.backend.repository;

import com.eventhive.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByUuid(String uuid);
    List<Ticket> findByEventId(Long eventId);
    List<Ticket> findByBookingId(Long bookingId);
}
