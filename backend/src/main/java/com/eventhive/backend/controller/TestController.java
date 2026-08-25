package com.eventhive.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import com.eventhive.backend.repository.UserRepository;
import com.eventhive.backend.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@RestController
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/api/test")
    public String testApi() {
        return "EventHive Backend is successfully running!";
    }

    @GetMapping("/api/reset-admin")
    public String resetAdmin() {
        Optional<User> userOpt = userRepository.findByEmail("admin@eventhive.com");
        if (userOpt.isPresent()) {
            User admin = userOpt.get();
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setStatus("APPROVED");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            return "Admin password reset successfully to Admin@123";
        }
        return "Admin user not found!";
    }
}