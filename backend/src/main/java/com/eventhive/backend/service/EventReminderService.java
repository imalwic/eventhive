package com.eventhive.backend.service;

import com.eventhive.backend.entity.Booking;
import com.eventhive.backend.entity.Event;
import com.eventhive.backend.repository.BookingRepository;
import com.eventhive.backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service that checks for upcoming events and sends
 * reminder emails to attendees approximately 24 hours before the event.
 *
 * Runs every hour (cron: "0 0 * * * *").
 */
@Service
public class EventReminderService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Runs every hour. Finds APPROVED events happening in the next 23–25 hour window
     * (to avoid sending multiple reminders) and emails all confirmed attendees.
     */
    @Scheduled(cron = "0 0 * * * *") // every hour at :00
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.plusHours(23);
        LocalDateTime windowEnd   = now.plusHours(25);

        System.out.println("[ReminderScheduler] Checking for events between " + windowStart + " and " + windowEnd);

        List<Event> upcomingEvents = eventRepository.findByStatusAndEventDateBetween("APPROVED", windowStart, windowEnd);

        for (Event event : upcomingEvents) {
            List<Booking> bookings = bookingRepository.findByEventAndStatus(event, "PAID");
            System.out.println("[ReminderScheduler] Sending reminders for event: " + event.getTitle()
                + " (" + bookings.size() + " attendees)");

            for (Booking booking : bookings) {
                try {
                    emailService.sendEventReminderEmail(booking.getUser(), event);
                } catch (Exception e) {
                    System.out.println("[ReminderScheduler] Failed for user: " + booking.getUser().getEmail());
                }
            }
        }
    }
}
