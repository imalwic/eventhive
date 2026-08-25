package com.eventhive.backend.controller;

import com.eventhive.backend.entity.Subscription;
import com.eventhive.backend.entity.User;
import com.eventhive.backend.repository.SubscriptionRepository;
import com.eventhive.backend.repository.UserRepository;
import com.eventhive.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;

    // Get current active subscription for organizer
    // Get current/latest subscription for organizer
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSubscription(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        // Get the latest subscription regardless of status
        List<Subscription> subs = subscriptionRepository.findByOrganizerOrderByCreatedAtDesc(userOpt.get());
        if (!subs.isEmpty()) {
            return ResponseEntity.ok(subs.get(0));
        }
        return ResponseEntity.ok(null);
    }

    // Purchase package (Card or Bank Slip)
    @PostMapping(value = "/purchase", consumes = { "multipart/form-data" })
    public ResponseEntity<?> purchasePackage(
            Authentication authentication,
            @RequestParam("packageName") String packageName,
            @RequestParam("price") Double price,
            @RequestParam("maxEvents") Integer maxEvents,
            @RequestParam("durationMonths") Integer durationMonths,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "paymentSlip", required = false) MultipartFile paymentSlip) {

        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        Subscription sub = new Subscription();
        sub.setOrganizer(userOpt.get());
        sub.setPackageName(packageName);
        sub.setPrice(price);
        sub.setMaxEvents(maxEvents);
        sub.setEventsUsed(0);
        sub.setPaymentMethod(paymentMethod);

        if ("CARD".equalsIgnoreCase(paymentMethod)) {
            // Online Card Payment via PayHere
            sub.setStatus("PENDING_PAYMENT");
            // Set dummy dates, will be updated upon successful payment
            sub.setStartDate(LocalDateTime.now());
            sub.setEndDate(LocalDateTime.now().plusMonths(durationMonths));
        } else {
            // Bank Slip - Requires Admin Approval
            if (paymentSlip == null || paymentSlip.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Payment slip is required for bank transfer."));
            }
            try {
                Path uploadPath = Paths.get("uploads", "payments");
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                
                String filename = System.currentTimeMillis() + "_" + paymentSlip.getOriginalFilename();
                Path filePath = uploadPath.resolve(filename);
                Files.copy(paymentSlip.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                
                sub.setPaymentProof(filename);
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(Map.of("message", "Failed to upload payment slip."));
            }
            sub.setStatus("PENDING");
            // Set dummy dates, will be updated on approval
            sub.setStartDate(LocalDateTime.now());
            sub.setEndDate(LocalDateTime.now().plusMonths(durationMonths)); 
        }

        Subscription saved = subscriptionRepository.save(sub);
        
        // Only send invoice immediately if it's a bank transfer (pending approval)
        if (!"CARD".equalsIgnoreCase(paymentMethod)) {
            emailService.sendPackageInvoiceEmail(userOpt.get(), saved);
        }
        
        return ResponseEntity.ok(saved);
    }

    // ADMIN ENDPOINTS
    @GetMapping("/admin/pending")
    public ResponseEntity<?> getPendingSubscriptions(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        // Assuming admin auth check here
        List<Subscription> pending = subscriptionRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/admin/invoice/{id}")
    public ResponseEntity<byte[]> getInvoicePdf(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        
        Optional<Subscription> subOpt = subscriptionRepository.findById(id);
        if (subOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Subscription sub = subOpt.get();
        byte[] pdfBytes = emailService.generateInvoicePdf(sub.getOrganizer(), sub);
        
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Invoice_" + sub.getPackageName() + ".pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<?> approveSubscription(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        
        return subscriptionRepository.findById(id).map(sub -> {
            sub.setStatus("ACTIVE");
            sub.setStartDate(LocalDateTime.now());
            
            // Need to know duration again, or we can just keep the original endDate logic if we saved duration temporarily
            // Let's just calculate duration from current endDate and startDate difference, or strictly set it based on packageName
            int months = 1;
            if ("BASIC".equalsIgnoreCase(sub.getPackageName())) months = 6;
            else if ("STANDARD".equalsIgnoreCase(sub.getPackageName())) months = 12;
            else if ("PRO".equalsIgnoreCase(sub.getPackageName())) months = 24;
            
            sub.setEndDate(LocalDateTime.now().plusMonths(months));
            Subscription savedSub = subscriptionRepository.save(sub);
            
            // Send approval email
            emailService.sendPackageStatusEmail(sub.getOrganizer(), savedSub, true);
            
            return ResponseEntity.ok(Map.of("message", "Subscription approved successfully."));
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/admin/reject/{id}")
    public ResponseEntity<?> rejectSubscription(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(401).build();
        
        return subscriptionRepository.findById(id).map(sub -> {
            sub.setStatus("REJECTED");
            Subscription savedSub = subscriptionRepository.save(sub);
            
            // Send rejection email
            emailService.sendPackageStatusEmail(sub.getOrganizer(), savedSub, false);
            
            return ResponseEntity.ok(Map.of("message", "Subscription rejected."));
        }).orElse(ResponseEntity.notFound().build());
    }
}
