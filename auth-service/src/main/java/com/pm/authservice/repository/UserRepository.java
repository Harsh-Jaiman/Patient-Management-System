package com.pm.authservice.repository;

import com.pm.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

// Repository to interact with User table
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find a user by email
    Optional<User> findByEmail(String email);
}
