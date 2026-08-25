package com.eventhive.backend.service;

import com.eventhive.backend.entity.Event;
import com.eventhive.backend.entity.User;
import com.eventhive.backend.entity.Waitlist;
import com.eventhive.backend.repository.EventRepository;
import com.eventhive.backend.repository.UserRepository;
import com.eventhive.backend.repository.WaitlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WaitlistService {

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Waitlist joinWaitlist(Long eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 🔥 මෙතන තමයි වෙනස් වුණේ! කලින් Notified වුණු අයට ආයෙත් පෝලිමට එන්න පුළුවන්.
        if (waitlistRepository.existsByUserAndEventAndNotifiedFalse(user, event)) {
            throw new RuntimeException("You are already on the active waitlist for this event.");
        }

        int currentMaxPosition = 0;
        Optional<Waitlist> lastInLine = waitlistRepository.findTopByEventOrderByPositionDesc(event);
        if (lastInLine.isPresent()) {
            currentMaxPosition = lastInLine.get().getPosition();
        }

        Waitlist waitlist = new Waitlist();
        waitlist.setUser(user);
        waitlist.setEvent(event);
        waitlist.setPosition(currentMaxPosition + 1);
        waitlist.setNotified(false);

        return waitlistRepository.save(waitlist);
    }

    @Transactional
    public void notifyNextPerson(Event event) {
        Optional<Waitlist> nextPerson = waitlistRepository.findTopByEventAndNotifiedFalseOrderByPositionAsc(event);

        if (nextPerson.isPresent()) {
            Waitlist waitlistEntry = nextPerson.get();
            waitlistEntry.setNotified(true);
            waitlistRepository.save(waitlistEntry);

            String userEmail = waitlistEntry.getUser().getEmail();
            String userName = waitlistEntry.getUser().getName();
            String eventName = event.getTitle();

            String subject = "Good News! A Seat is Available for " + eventName;
            String text = "Hi " + userName + ",\n\n" +
                    "A seat has just become available for the event '" + eventName + "'!\n\n" +
                    "Since you are next on the waitlist, please log in to EventHive to book your ticket before someone else grabs it.\n\n" +
                    "Thank you,\nEventHive Team";

            emailService.sendEmail(userEmail, subject, text);
            System.out.println("Waitlist Alert: A seat is available for event '" + eventName + "'. Notified user: " + userEmail);
        }
    }

    public java.util.List<Waitlist> getUserWaitlists(String userEmail) {
        return waitlistRepository.findByUser_Email(userEmail);
    }
}