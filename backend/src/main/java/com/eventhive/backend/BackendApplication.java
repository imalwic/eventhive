package com.eventhive.backend;

import com.eventhive.backend.entity.User;
import com.eventhive.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findByEmail("admin@eventhive.com").isEmpty()) {
				User admin = new User();
				admin.setName("Admin");
				admin.setEmail("admin@eventhive.com");
				admin.setPhone("0000000000");
				admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
				admin.setRole("ADMIN");
				admin.setStatus("APPROVED");
				
				userRepository.save(admin);
				System.out.println("Default Admin Account Created! (admin@eventhive.com / Admin@123)");
			}
		};
	}
}
