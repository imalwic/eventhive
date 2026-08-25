package com.eventhive.backend.service;

import com.eventhive.backend.dto.BookingRequest;
import com.eventhive.backend.entity.Booking;
import com.eventhive.backend.entity.Event;
import com.eventhive.backend.entity.Seat;
import com.eventhive.backend.entity.User;
import com.eventhive.backend.repository.BookingRepository;
import com.eventhive.backend.repository.EventRepository;
import com.eventhive.backend.repository.SeatRepository;
import com.eventhive.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eventhive.backend.repository.TicketRepository;
import com.eventhive.backend.service.EmailService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private WaitlistService waitlistService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Booking createBooking(BookingRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<Seat> requestedSeats = seatRepository.findAllById(request.getSeatIds());

        if (requestedSeats.size() != request.getSeatIds().size()) {
            throw new RuntimeException("Some seats are invalid or not found!");
        }

        double totalAmount = 0.0;
        java.util.Set<Seat> finalSeatsToBook = new java.util.HashSet<>();

        List<Seat> allEventSeats = seatRepository.findByEventId(event.getId());

        // Process all tables first
        for (Seat seat : requestedSeats) {
            if ("TABLE".equals(seat.getSeatType())) {
                finalSeatsToBook.add(seat);
                
                int surroundingChairsCount = 0;
                for (Seat other : allEventSeats) {
                    if ("CHAIR".equals(other.getSeatType())) {
                        double dx = Math.abs(other.getXCoordinate() - seat.getXCoordinate());
                        double dy = Math.abs(other.getYCoordinate() - seat.getYCoordinate());
                        
                        if (dx <= 1.5 && dy <= 1.5) {
                            if (!other.getStatus().equals("AVAILABLE")) {
                                throw new RuntimeException("A chair attached to this table is already taken!");
                            }
                            finalSeatsToBook.add(other);
                            surroundingChairsCount++;
                        }
                    }
                }
                
                if (surroundingChairsCount > 0) {
                    // Special Offer: Buy N, Get 1 Free for the group
                    totalAmount += Math.max(0, surroundingChairsCount - 1) * seat.getPrice();
                } else {
                    totalAmount += seat.getPrice();
                }
            }
        }

        // Now process remaining chairs/seats that were NOT part of any table
        for (Seat seat : requestedSeats) {
            if (!"TABLE".equals(seat.getSeatType())) {
                // If it was already added by a table group, skip it (don't charge again)
                if (finalSeatsToBook.contains(seat)) continue;

                if (!seat.getStatus().equals("AVAILABLE")) {
                    throw new RuntimeException("Seat " + seat.getSeatNumber() + " is already taken or locked");
                }
                finalSeatsToBook.add(seat);
                totalAmount += seat.getPrice();
            }
        }

        // Mark all as booked
        for (Seat s : finalSeatsToBook) {
            s.setStatus("BOOKED");
        }

        seatRepository.saveAll(finalSeatsToBook);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setSeats(requestedSeats);
        booking.setTotalAmount(totalAmount);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus("PENDING_PAYMENT"); // Initially pending until PayHere success

        return bookingRepository.save(booking);
    }

    @Transactional
    public void generateTicketsForBooking(Long bookingId) {
        Booking savedBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Event event = savedBooking.getEvent();
        User user = savedBooking.getUser();
        List<Seat> requestedSeats = savedBooking.getSeats();

        // 🎟️ TICKET GENERATION LOGIC 🎟️
        java.util.List<com.eventhive.backend.entity.Ticket> generatedTickets = new java.util.ArrayList<>();
        
        // Group the booked seats to handle TABLE vs CHAIR
        for (Seat seat : requestedSeats) {
            if ("TABLE".equals(seat.getSeatType())) {
                // Table gets ONE unified ticket
                com.eventhive.backend.entity.Ticket t = new com.eventhive.backend.entity.Ticket();
                t.setUuid(java.util.UUID.randomUUID().toString());
                t.setBooking(savedBooking);
                t.setEvent(event);
                
                // Find matching ticket category based on tier name
                com.eventhive.backend.entity.TicketCategory cat = event.getTicketCategories().stream()
                        .filter(c -> c.getName().equals(seat.getTierName()))
                        .findFirst()
                        .orElse(event.getTicketCategories().get(0)); // Fallback
                t.setTicketCategory(cat);
                t.setIsGroupTicket(true);
                generatedTickets.add(t);
            } else if ("CHAIR".equals(seat.getSeatType())) {
                // Check if this chair is part of a table group in this booking
                boolean isPartOfTable = false;
                for (Seat other : requestedSeats) {
                    if ("TABLE".equals(other.getSeatType())) {
                        double dx = Math.abs(other.getXCoordinate() - seat.getXCoordinate());
                        double dy = Math.abs(other.getYCoordinate() - seat.getYCoordinate());
                        if (dx <= 1.5 && dy <= 1.5) {
                            isPartOfTable = true;
                            break;
                        }
                    }
                }
                
                // If it's an independent chair, give it its own ticket
                if (!isPartOfTable) {
                    com.eventhive.backend.entity.Ticket t = new com.eventhive.backend.entity.Ticket();
                    t.setUuid(java.util.UUID.randomUUID().toString());
                    t.setBooking(savedBooking);
                    t.setEvent(event);
                    
                    com.eventhive.backend.entity.TicketCategory cat = event.getTicketCategories().stream()
                            .filter(c -> c.getName().equals(seat.getTierName()))
                            .findFirst()
                            .orElse(event.getTicketCategories().get(0));
                    t.setTicketCategory(cat);
                    t.setIsGroupTicket(false);
                    generatedTickets.add(t);
                }
            } else {
                // Other types (e.g. Standing Zone) get individual tickets
                com.eventhive.backend.entity.Ticket t = new com.eventhive.backend.entity.Ticket();
                t.setUuid(java.util.UUID.randomUUID().toString());
                t.setBooking(savedBooking);
                t.setEvent(event);
                
                com.eventhive.backend.entity.TicketCategory cat = event.getTicketCategories().stream()
                        .filter(c -> c.getName().equals(seat.getTierName()))
                        .findFirst()
                        .orElse(event.getTicketCategories().get(0));
                t.setTicketCategory(cat);
                t.setIsGroupTicket(false);
                generatedTickets.add(t);
            }
        }
        
        ticketRepository.saveAll(generatedTickets);
        
        // 📧 SEND EMAIL LOGIC 📧
        emailService.sendTicketReceipt(user, savedBooking, generatedTickets);
    }

    public List<Booking> getUserBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingRepository.findByUser(user);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to cancel this booking!");
        }

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("This booking is already cancelled.");
        }

        List<Seat> seats = booking.getSeats();
        for (Seat seat : seats) {
            seat.setStatus("AVAILABLE");
        }
        seatRepository.saveAll(seats);

        booking.setStatus("CANCELLED");
        Booking savedBooking = bookingRepository.save(booking);

        // 🔥 මාරම වැදගත් කෑල්ල: ටිකට් එක Cancel වුණු ගමන්, Waitlist එකේ ඉන්න කෙනාට ඔටෝම Alert එක යවනවා
        waitlistService.notifyNextPerson(booking.getEvent());

        return savedBooking;
    }
}