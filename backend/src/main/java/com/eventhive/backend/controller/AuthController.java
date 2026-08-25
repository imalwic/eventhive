package com.eventhive.backend.controller;

import com.eventhive.backend.security.JwtUtil;
import com.eventhive.backend.repository.UserRepository;
import com.eventhive.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            // 1. check the email and password are correctly
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // 2. details are correct after create the new token
            String token = jwtUtil.generateToken(email);

            // 3. users to send the token
            return ResponseEntity.ok(Map.of("token", token));

        } catch (AuthenticationException e) {
            // Error come the password or email incorrect
            return ResponseEntity.status(401).body("Invalid email or password!");
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> payload) {
        try {
            String idTokenString = payload.get("credential");
            
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = 
                new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(), 
                    new com.google.api.client.json.gson.GsonFactory())
                .setAudience(java.util.Collections.singletonList("313163096616-iect6906cnhpnh07u3bpj3a3p80nj3uq.apps.googleusercontent.com")) 
                .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken != null) {
                com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload googlePayload = idToken.getPayload();
                String email = googlePayload.getEmail();
                String name = (String) googlePayload.get("name");
                
                // Check if user exists
                java.util.Optional<com.eventhive.backend.entity.User> existingUserOpt = userRepository.findByEmail(email);
                if (existingUserOpt.isEmpty()) {
                    // Create new user automatically as ATTENDEE
                    com.eventhive.backend.entity.User newUser = new com.eventhive.backend.entity.User();
                    newUser.setEmail(email);
                    newUser.setName(name != null ? name : "Google User");
                    newUser.setRole("ATTENDEE");
                    newUser.setStatus("APPROVED");
                    
                    // Dummy phone and password for Google users
                    newUser.setPhone("0000000000"); 
                    String randomPassword = java.util.UUID.randomUUID().toString();
                    newUser.setPasswordHash(passwordEncoder.encode(randomPassword));
                    
                    userRepository.save(newUser);

                    // Send Welcome Email
                    String subject = "Welcome to EventHive!";
                    String body = "Hi " + newUser.getName() + ",\n\nSuccessfully registered! Start discovering and enjoying events on EventHive.\n\nBest regards,\nEventHive Team";
                    emailService.sendEmail(newUser.getEmail(), subject, body);
                }
                
                // Generate token
                String token = jwtUtil.generateToken(email);
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                return ResponseEntity.status(401).body("Invalid Google ID Token.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing Google login.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            java.util.Optional<com.eventhive.backend.entity.User> existingUserOpt = userRepository.findByEmail(email);

            if (existingUserOpt.isPresent()) {
                com.eventhive.backend.entity.User user = existingUserOpt.get();
                // Generate 6 digit OTP
                String otp = String.format("%06d", new java.util.Random().nextInt(999999));
                user.setResetOtp(otp);
                user.setResetOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
                userRepository.save(user);

                // Send email
                String subject = "EventHive Password Reset OTP";
                String body = "Hi " + user.getName() + ",\n\nYour OTP for password reset is: " + otp + "\n\nThis OTP is valid for 5 minutes.\n\nBest regards,\nEventHive Team";
                emailService.sendEmail(user.getEmail(), subject, body);
            }
            
            // Always return success to prevent email enumeration
            return ResponseEntity.ok(Map.of("message", "If your email is registered, an OTP has been sent."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing forgot password request.");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String otp = payload.get("otp");
            java.util.Optional<com.eventhive.backend.entity.User> existingUserOpt = userRepository.findByEmail(email);

            if (existingUserOpt.isPresent()) {
                com.eventhive.backend.entity.User user = existingUserOpt.get();
                if (user.getResetOtp() != null && user.getResetOtp().equals(otp)) {
                    if (user.getResetOtpExpiry() != null && user.getResetOtpExpiry().isAfter(java.time.LocalDateTime.now())) {
                        return ResponseEntity.ok(Map.of("message", "OTP verified successfully."));
                    } else {
                        return ResponseEntity.status(400).body(Map.of("message", "OTP has expired."));
                    }
                } else {
                    return ResponseEntity.status(400).body(Map.of("message", "Invalid OTP."));
                }
            }
            return ResponseEntity.status(400).body(Map.of("message", "Invalid request."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error verifying OTP.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String otp = payload.get("otp");
            String newPassword = payload.get("newPassword");
            java.util.Optional<com.eventhive.backend.entity.User> existingUserOpt = userRepository.findByEmail(email);

            if (existingUserOpt.isPresent()) {
                com.eventhive.backend.entity.User user = existingUserOpt.get();
                if (user.getResetOtp() != null && user.getResetOtp().equals(otp) && user.getResetOtpExpiry() != null && user.getResetOtpExpiry().isAfter(java.time.LocalDateTime.now())) {
                    user.setPasswordHash(passwordEncoder.encode(newPassword));
                    user.setResetOtp(null);
                    user.setResetOtpExpiry(null);
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
                } else {
                    return ResponseEntity.status(400).body(Map.of("message", "Invalid or expired OTP."));
                }
            }
            return ResponseEntity.status(400).body(Map.of("message", "Invalid request."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error resetting password.");
        }
    }
}