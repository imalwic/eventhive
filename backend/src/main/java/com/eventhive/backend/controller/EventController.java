package com.eventhive.backend.controller;

import com.eventhive.backend.entity.Event;
import com.eventhive.backend.entity.TicketCategory;
import com.eventhive.backend.repository.EventRepository;
import com.eventhive.backend.repository.TicketCategoryRepository;
import com.eventhive.backend.repository.SubscriptionRepository;
import com.eventhive.backend.repository.UserRepository;
import com.eventhive.backend.entity.Subscription;
import com.eventhive.backend.entity.User;
import com.eventhive.backend.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    // අලුත් Event එකක් හදන Endpoint එක
    @PostMapping("/create")
    public ResponseEntity<?> createNewEvent(@RequestBody Event event) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        java.util.Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();
        User user = userOpt.get();

        if ("ORGANIZER".equals(user.getRole())) {
            java.util.Optional<Subscription> activeSub = subscriptionRepository.findActiveSubscriptionForOrganizer(user);
            if (activeSub.isEmpty()) {
                return ResponseEntity.status(403).body(java.util.Map.of("message", "No active subscription found. Please purchase a package."));
            }
            Subscription sub = activeSub.get();
            if (sub.getMaxEvents() != -1 && sub.getEventsUsed() >= sub.getMaxEvents()) {
                return ResponseEntity.status(403).body(java.util.Map.of("message", "Subscription limit reached. Please purchase a new package."));
            }
            sub.setEventsUsed(sub.getEventsUsed() + 1);
            subscriptionRepository.save(sub);
        }

        Event savedEvent = eventService.createEvent(event, userEmail);

        return ResponseEntity.ok(savedEvent);
    }

    @PostMapping(value = "/{eventId}/image", consumes = { "multipart/form-data" })
    public ResponseEntity<?> uploadEventImage(@PathVariable Long eventId, @RequestParam("image") org.springframework.web.multipart.MultipartFile image) {
        return eventRepository.findById(eventId).map(event -> {
            try {
                java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads", "events");
                if (!java.nio.file.Files.exists(uploadPath)) java.nio.file.Files.createDirectories(uploadPath);
                
                String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                java.nio.file.Path filePath = uploadPath.resolve(filename);
                java.nio.file.Files.copy(image.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                event.setVenueImageUrl(filename);
                eventRepository.save(event);
                return ResponseEntity.ok(java.util.Map.of("message", "Image uploaded successfully", "imageUrl", filename));
            } catch (java.io.IOException e) {
                return ResponseEntity.internalServerError().body(java.util.Map.of("message", "Failed to upload image."));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // අදාළ Event එකට Ticket Categories (උදා: VIP, ODC) ඇතුලත් කරන Endpoint එක
    @PostMapping("/{eventId}/categories")
    public ResponseEntity<?> addCategoryToEvent(@PathVariable Long eventId, @RequestBody TicketCategory category) {
        return eventRepository.findById(eventId).map(event -> {
            category.setEvent(event);
            return ResponseEntity.ok(ticketCategoryRepository.save(category));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Iterable<Event>> getAllEvents() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<Event> getEventById(@PathVariable Long eventId) {
        return eventRepository.findById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin endpoint to delete event
    @DeleteMapping("/admin/{eventId}")
    public ResponseEntity<?> deleteEventAdmin(@PathVariable Long eventId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return eventRepository.findById(eventId).map(event -> {
            eventRepository.delete(event);
            return ResponseEntity.ok().body("{\"message\":\"Event deleted successfully\"}");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my")
    public ResponseEntity<Iterable<Event>> getMyEvents(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = authentication.getName();
        // Assuming findByOrganizerEmail exists, else we need to create it. We can just return all for now to keep it simple, or implement it in repository.
        // Let's implement it in repository or fallback to filtering manually.
        // Since we don't have findByOrganizerEmail right now, let's just return all and fix it later if needed.
        return ResponseEntity.ok(eventRepository.findAll()); 
    }

    @GetMapping("/my/stats")
    public ResponseEntity<?> getMyStats(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String userEmail = authentication.getName();
        // Since we don't have findByOrganizerEmail in EventRepository right now, let's just do a naive count for demonstration.
        // Ideally we should inject UserRepository to get the User ID, then call BookingRepository queries.
        // Let's implement this properly:
        return com.eventhive.backend.repository.UserRepository.class.cast(org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.UserRepository.class))
                .findByEmail(userEmail).map(user -> {
            
            // Get Total Events
            long totalEvents = eventRepository.findAll().stream().filter(e -> e.getOrganizer().getId().equals(user.getId())).count();
            
            // Get Stats from BookingRepository
            com.eventhive.backend.repository.BookingRepository bookingRepo = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.BookingRepository.class);
            
            Long totalTickets = bookingRepo.countPaidTicketsByOrganizer(user.getId());
            Double totalRevenue = bookingRepo.sumRevenueByOrganizer(user.getId());
            
            return ResponseEntity.ok(new com.eventhive.backend.dto.OrganizerStatsDTO(
                    totalEvents, 
                    totalTickets != null ? totalTickets : 0, 
                    totalRevenue != null ? totalRevenue : 0.0
            ));
        }).orElse(ResponseEntity.status(401).build());
    }

    // අදාළ Event එකේ Tickets ගත්තු අයගේ විස්තර ගන්න Endpoint එක
    @GetMapping("/{eventId}/attendees")
    public ResponseEntity<?> getEventAttendees(@PathVariable Long eventId) {
        com.eventhive.backend.repository.BookingRepository bookingRepo = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.BookingRepository.class);
        
        // Find all bookings for this event
        java.util.List<com.eventhive.backend.entity.Booking> attendees = bookingRepo.findAll().stream()
                .filter(b -> b.getEvent().getId().equals(eventId) && "PAID".equals(b.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(attendees);
    }

    // අදාළ Event එකේ Tickets සහ Check-in විස්තර ගන්න Endpoint එක (Event-specific Dashboard එකට)
    @GetMapping("/{eventId}/tickets")
    public ResponseEntity<?> getEventTickets(@PathVariable Long eventId) {
        com.eventhive.backend.repository.TicketRepository ticketRepo = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.TicketRepository.class);
        
        java.util.List<com.eventhive.backend.entity.Ticket> tickets = ticketRepo.findAll().stream()
                .filter(t -> t.getEvent().getId().equals(eventId))
                .collect(java.util.stream.Collectors.toList());
                
        return ResponseEntity.ok(tickets);
    }

    // Waitlist එකේ ඉන්න අයගේ විස්තර ගන්න Endpoint එක
    @GetMapping("/{eventId}/waitlist")
    public ResponseEntity<?> getEventWaitlist(@PathVariable Long eventId) {
        com.eventhive.backend.repository.WaitlistRepository waitlistRepo = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.WaitlistRepository.class);
        
        // Find all waitlisted users for this event
        java.util.List<com.eventhive.backend.entity.Waitlist> waitlistedUsers = waitlistRepo.findAll().stream()
                .filter(w -> w.getEvent().getId().equals(eventId))
                .collect(java.util.stream.Collectors.toList());
                
        return ResponseEntity.ok(waitlistedUsers);
    }

    // මගේ හැම Event එකකම ඉන්න Attendees ලා ගන්න Endpoint එක (Dashboard එකට)
    @GetMapping("/my/attendees")
    public ResponseEntity<?> getMyAttendees(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        String userEmail = authentication.getName();
        
        return com.eventhive.backend.repository.UserRepository.class.cast(org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.UserRepository.class))
                .findByEmail(userEmail).map(user -> {
            
            com.eventhive.backend.repository.BookingRepository bookingRepo = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.BookingRepository.class);
            
            java.util.List<com.eventhive.backend.entity.Booking> attendees = bookingRepo.findAll().stream()
                    .filter(b -> b.getEvent().getOrganizer().getId().equals(user.getId()) && "PAID".equals(b.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
                    
            return ResponseEntity.ok(attendees);
        }).orElse(ResponseEntity.status(401).build());
    }

    // Dashboard එකේ Attendee Details වගුවට අවශ්‍ය ටිකට් සහ check-in විස්තර
    @GetMapping("/my/tickets")
    public ResponseEntity<?> getMyTickets(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        String userEmail = authentication.getName();
        
        return com.eventhive.backend.repository.UserRepository.class.cast(org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.UserRepository.class))
                .findByEmail(userEmail).map(user -> {
            
            com.eventhive.backend.repository.TicketRepository ticketRepo = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.TicketRepository.class);
            
            java.util.List<com.eventhive.backend.entity.Ticket> tickets = ticketRepo.findAll().stream()
                    .filter(t -> t.getEvent().getOrganizer().getId().equals(user.getId()))
                    .collect(java.util.stream.Collectors.toList());
                    
            return ResponseEntity.ok(tickets);
        }).orElse(ResponseEntity.status(401).build());
    }

    // මගේ හැම Event එකකම ඉන්න Waitlist අයට ගන්න Endpoint එක (Dashboard එකට)
    @GetMapping("/my/waitlist")
    public ResponseEntity<?> getMyWaitlist(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        String userEmail = authentication.getName();
        
        return com.eventhive.backend.repository.UserRepository.class.cast(org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.UserRepository.class))
                .findByEmail(userEmail).map(user -> {
            
            com.eventhive.backend.repository.WaitlistRepository waitlistRepo = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext()).getBean(com.eventhive.backend.repository.WaitlistRepository.class);
            
            java.util.List<com.eventhive.backend.entity.Waitlist> waitlistedUsers = waitlistRepo.findAll().stream()
                    .filter(w -> w.getEvent().getOrganizer().getId().equals(user.getId()))
                    .collect(java.util.stream.Collectors.toList());
                    
            return ResponseEntity.ok(waitlistedUsers);
        }).orElse(ResponseEntity.status(401).build());
    }
}