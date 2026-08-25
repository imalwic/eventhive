package com.eventhive.backend.repository;

import com.eventhive.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // find the user used email
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndStatus(String role, String status);
    List<User> findByRole(String role);
    Long countByRole(String role);
}