package com.eventhive.backend.repository;

import com.eventhive.backend.entity.Booking;
import com.eventhive.backend.entity.Event;
import com.eventhive.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // User හරහා එයාගේ Bookings හොයන අලුත් Method එක
    List<Booking> findByUser(User user);

    // Get total tickets sold for events hosted by a specific organizer where booking is PAID
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(b) FROM Booking b WHERE b.event.organizer.id = :organizerId AND b.status = 'PAID'")
    Long countPaidTicketsByOrganizer(@org.springframework.data.repository.query.Param("organizerId") Long organizerId);

    // Get total revenue for events hosted by a specific organizer where booking is PAID
    @org.springframework.data.jpa.repository.Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.event.organizer.id = :organizerId AND b.status = 'PAID'")
    Double sumRevenueByOrganizer(@org.springframework.data.repository.query.Param("organizerId") Long organizerId);

    // Get total platform revenue
    @org.springframework.data.jpa.repository.Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'PAID'")
    Double sumTotalRevenue();

    // For reminder emails: all PAID bookings for a given event
    List<Booking> findByEventAndStatus(Event event, String status);
}