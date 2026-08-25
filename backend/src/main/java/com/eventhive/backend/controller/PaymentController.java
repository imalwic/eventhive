package com.eventhive.backend.controller;

import com.eventhive.backend.entity.Booking;
import com.eventhive.backend.entity.Subscription;
import com.eventhive.backend.repository.BookingRepository;
import com.eventhive.backend.repository.SubscriptionRepository;
import com.eventhive.backend.service.PaymentService;
import com.eventhive.backend.service.BookingService;
import com.eventhive.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EmailService emailService;

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @GetMapping("/generate-hash/{bookingId}")
    public ResponseEntity<?> generatePaymentDetails(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            double amount = booking.getTotalAmount();
            String currency = "LKR";
            String orderId = "BKG-" + booking.getId();

            String hash = paymentService.generatePaymentHash(orderId, amount, currency);

            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("merchant_id", merchantId);
            paymentDetails.put("order_id", orderId);
            paymentDetails.put("amount", amount);
            paymentDetails.put("currency", currency);
            paymentDetails.put("hash", hash);

            return ResponseEntity.ok(paymentDetails);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/generate-hash/subscription/{subId}")
    public ResponseEntity<?> generateSubscriptionHash(@PathVariable Long subId) {
        try {
            Subscription sub = subscriptionRepository.findById(subId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            double amount = sub.getPrice();
            String currency = "LKR";
            String orderId = "SUB-" + sub.getId();

            String hash = paymentService.generatePaymentHash(orderId, amount, currency);

            Map<String, Object> paymentDetails = new HashMap<>();
            paymentDetails.put("merchant_id", merchantId);
            paymentDetails.put("order_id", orderId);
            paymentDetails.put("amount", amount);
            paymentDetails.put("currency", currency);
            paymentDetails.put("hash", hash);

            return ResponseEntity.ok(paymentDetails);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PayHere Callback / Notify API
    @PostMapping("/notify")
    public ResponseEntity<?> payHereNotify(
            @RequestParam("merchant_id") String merchantId,
            @RequestParam("order_id") String orderId,
            @RequestParam("payhere_amount") String payhereAmount,
            @RequestParam("payhere_currency") String payhereCurrency,
            @RequestParam("status_code") String statusCode,
            @RequestParam("md5sig") String md5sig) {

        try {
            // Validate md5sig here ideally (skipped for brevity/testing)

            if (orderId != null && orderId.startsWith("BKG-")) {
                Long bookingId = Long.parseLong(orderId.replace("BKG-", ""));
                Booking booking = bookingRepository.findById(bookingId).orElse(null);

                if (booking != null) {
                    if ("2".equals(statusCode)) {
                        booking.setStatus("PAID");
                        bookingRepository.save(booking);
                        
                        // Payment successful, now generate tickets and email receipt!
                        try {
                            bookingService.generateTicketsForBooking(bookingId);
                        } catch (Exception e) {
                            System.err.println("Error generating tickets after payment: " + e.getMessage());
                        }

                        System.out.println("Payment Successful and Tickets Generated for Order: " + orderId);
                    } else {
                        booking.setStatus("FAILED");
                        bookingRepository.save(booking);
                    }
                }
            } else if (orderId != null && orderId.startsWith("SUB-")) {
                Long subId = Long.parseLong(orderId.replace("SUB-", ""));
                Subscription sub = subscriptionRepository.findById(subId).orElse(null);

                if (sub != null) {
                    if ("2".equals(statusCode)) {
                        sub.setStatus("PENDING"); // Admin approval required even for online
                        
                        // Send the invoice email (sent to Organizer and BCC Admin)
                        emailService.sendPackageInvoiceEmail(sub.getOrganizer(), sub);
                        
                        subscriptionRepository.save(sub);
                        System.out.println("Payment Successful and Subscription Pending Approval for Order: " + orderId);
                    } else {
                        sub.setStatus("FAILED");
                        subscriptionRepository.save(sub);
                    }
                }
            }

            return ResponseEntity.ok().body("Notification received successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing notification: " + e.getMessage());
        }
    }
}