package com.eventhive.backend.controller;

import com.eventhive.backend.entity.User;
import com.eventhive.backend.repository.UserRepository;
import com.eventhive.backend.repository.BookingRepository;
import com.eventhive.backend.repository.EventRepository;
import com.eventhive.backend.repository.SubscriptionRepository;
import com.eventhive.backend.entity.Subscription;
import com.eventhive.backend.service.EmailService;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.time.YearMonth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @PostMapping(value = "/create", consumes = { "multipart/form-data" })
    public ResponseEntity<?> createUser(
            @ModelAttribute User user,
            @RequestParam(value = "proposalFile", required = false) org.springframework.web.multipart.MultipartFile proposalFile) {
        
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        if ("ORGANIZER".equalsIgnoreCase(user.getRole())) {
            if (proposalFile == null || proposalFile.isEmpty()) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Event proposal PDF is mandatory for Organizers."));
            }
            
            try {
                // Define the uploads directory path
                java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads", "proposals");
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }
                
                // Save the file
                String filename = System.currentTimeMillis() + "_" + proposalFile.getOriginalFilename();
                java.nio.file.Path filePath = uploadPath.resolve(filename);
                java.nio.file.Files.copy(proposalFile.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                user.setEventProposal(filename);
            } catch (java.io.IOException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body(java.util.Map.of("message", "Failed to upload event proposal file."));
            }
            
            user.setStatus("PENDING");
        } else {
            user.setStatus("APPROVED");
        }
        
        User savedUser = userRepository.save(user);

        // Send Email based on role
        if ("ATTENDEE".equalsIgnoreCase(user.getRole())) {
            String subject = "Welcome to EventHive!";
            String body = "Hi " + user.getName() + ",\n\nSuccessfully registered! Start discovering and enjoying events on EventHive.\n\nBest regards,\nEventHive Team";
            emailService.sendEmail(user.getEmail(), subject, body);
        } else if ("ORGANIZER".equalsIgnoreCase(user.getRole())) {
            // Email to organizer
            String subject = "EventHive - Organizer Request Received";
            String body = "Hi " + user.getName() + ",\n\nWe have received your event proposal. Our admin team will review it. You will be notified once approved.\n\nBest regards,\nEventHive Team";
            emailService.sendEmail(user.getEmail(), subject, body);
            
            // Email to admin
            String adminSubject = "New Organizer Proposal: " + user.getName();
            String adminBody = "A new organizer has registered.\nName: " + user.getName() + "\nEmail: " + user.getEmail() + "\nProposal File: " + user.getEventProposal();
            emailService.sendEmail("admin@eventhive.com", adminSubject, adminBody); // Assuming admin email
        }

        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<User>> getPendingOrganizers(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        // Ideally verify authentication role is ADMIN here
        List<User> pendingUsers = userRepository.findByRoleAndStatus("ORGANIZER", "PENDING");
        return ResponseEntity.ok(pendingUsers);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveOrganizer(@PathVariable Long id, org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        // Ideally verify authentication role is ADMIN here
        return userRepository.findById(id).map(user -> {
            user.setStatus("APPROVED");
            userRepository.save(user);
            
            // Auto-provision FREE subscription (1 month, 1 event)
            Subscription freeSub = new Subscription();
            freeSub.setOrganizer(user);
            freeSub.setPackageName("FREE");
            freeSub.setPrice(0.0);
            freeSub.setMaxEvents(1);
            freeSub.setEventsUsed(0);
            freeSub.setStatus("ACTIVE");
            freeSub.setPaymentMethod("SYSTEM_GRANT");
            freeSub.setStartDate(LocalDateTime.now());
            freeSub.setEndDate(LocalDateTime.now().plusMonths(1));
            subscriptionRepository.save(freeSub);

            // Notify Organizer
            String subject = "EventHive - Organizer Account Approved!";
            String body = "Hi " + user.getName() + ",\n\nYour event proposal has been approved! You can now log in using your registered email and password to access the Organizer Dashboard and create your events.\n\nLogin URL: http://localhost:3000/login\n\nBest regards,\nEventHive Team";
            emailService.sendEmail(user.getEmail(), subject, body);
            
            return ResponseEntity.ok().body("{\"message\":\"Organizer approved successfully\"}");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectOrganizer(@PathVariable Long id, org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return userRepository.findById(id).map(user -> {
            user.setStatus("REJECTED");
            userRepository.save(user);
            
            String subject = "EventHive - Organizer Account Update";
            String body = "Hi " + user.getName() + ",\n\nUnfortunately, your event proposal was not approved by our admin team at this time.\n\nBest regards,\nEventHive Team";
            emailService.sendEmail(user.getEmail(), subject, body);
            
            return ResponseEntity.ok().body("{\"message\":\"Organizer rejected\"}");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/organizers")
    public ResponseEntity<List<User>> getOrganizers(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userRepository.findByRole("ORGANIZER"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok().body("{\"message\":\"User deleted successfully\"}");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<?> getAdminStats(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        Double totalRevenue = subscriptionRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = 0.0;

        Long totalOrganizers = userRepository.countByRole("ORGANIZER");
        long totalEvents = eventRepository.count();

        return ResponseEntity.ok(java.util.Map.of(
            "totalRevenue", totalRevenue,
            "totalOrganizers", totalOrganizers,
            "totalEvents", totalEvents
        ));
    }

    @GetMapping("/admin/chart-data")
    public ResponseEntity<?> getAdminChartData(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();

        // 1. Generate last 6 months labels
        List<String> last6Months = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            last6Months.add(currentMonth.minusMonths(i).format(DateTimeFormatter.ofPattern("MMM")));
        }

        // 2. Calculate Revenue Data
        List<Subscription> paidSubs = subscriptionRepository.findByStatusIn(List.of("ACTIVE", "EXPIRED"));
        Map<String, Double> revenueMap = new LinkedHashMap<>();
        for (String m : last6Months) revenueMap.put(m, 0.0);

        for (Subscription sub : paidSubs) {
            String month = sub.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM"));
            if (revenueMap.containsKey(month)) {
                revenueMap.put(month, revenueMap.get(month) + sub.getPrice());
            }
        }
        
        List<Map<String, Object>> revenueData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : revenueMap.entrySet()) {
            revenueData.add(Map.of("name", entry.getKey(), "revenue", entry.getValue()));
        }

        // 3. Package Distribution Data
        Map<String, Long> packageCount = paidSubs.stream()
            .filter(s -> !"FREE".equals(s.getPackageName())) // Exclude free
            .collect(Collectors.groupingBy(Subscription::getPackageName, Collectors.counting()));
        
        List<Map<String, Object>> packageData = new ArrayList<>();
        for (Map.Entry<String, Long> entry : packageCount.entrySet()) {
            packageData.add(Map.of("name", entry.getKey(), "value", entry.getValue()));
        }

        return ResponseEntity.ok(Map.of(
            "revenueData", revenueData,
            "packageData", packageData
        ));
    }
}