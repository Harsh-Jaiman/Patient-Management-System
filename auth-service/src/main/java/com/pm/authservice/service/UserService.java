package com.pm.authservice.service;

import com.pm.authservice.model.User;
import com.pm.authservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository; // Inject UserRepository to access DB

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Find a user by email.
     * @param email - Email of the user to search in database
     * @return Optional<User> - contains user if found, empty if not found
     */
    public Optional<User> findByEmail(String email) {
        // Delegate the call to UserRepository
        // JPA automatically generates SQL to find user by email
        return userRepository.findByEmail(email);
    }


}
