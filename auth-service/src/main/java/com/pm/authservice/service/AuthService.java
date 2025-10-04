package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;        // Service to fetch user data from DB
    private final PasswordEncoder passwordEncoder; // To check hashed passwords
    private final JwtUtil jwtUtil;                 // Utility to generate JWT tokens

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticate user credentials and generate tokens.
     * @param loginRequestDTO contains email and password from client
     * @return Optional containing LoginResponseDTO with access & refresh tokens if valid, empty if invalid
     */
    public Optional<LoginResponseDTO> authenticate(LoginRequestDTO loginRequestDTO) {

        // 1️⃣ Find user by email in database
        return userService.findByEmail(loginRequestDTO.getEmail())
                // 2️⃣ Check if the password provided matches the hashed password in DB
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
                // 3️⃣ If user exists and password matches, generate tokens
                .map(u -> {
                    // Generate a short-lived access token (30 mins)
                    String accessToken = jwtUtil.generateAccessToken(u.getEmail(), u.getRole());

                    // Generate a long-lived refresh token (10 days)
                    String refreshToken = jwtUtil.generateRefreshToken(u.getEmail(), u.getRole());

                    // 4️⃣ Wrap both tokens in a LoginResponseDTO and return
                    return new LoginResponseDTO(accessToken, refreshToken);
                });

        // 🔹 If user not found or password mismatch, Optional will be empty
    }

    /**
     * Token validation is corrected: it returns true only if JwtUtil completes without throwing an exception.
     */
    public boolean validateAccessToken(String token) {
        try {
            // Call JwtUtil to check token validity. This will now throw JwtException on failure.
            jwtUtil.validateAccessToken(token);
            return true; // If no exception, token is valid
        } catch (JwtException jwtException) {
            // If any issue (expired, tampered, invalid signature), return false
            System.out.println("Access Token validation failed: " + jwtException.getMessage());
            return false;
        }
    }

    /**
     * Token validation is corrected: it returns true only if JwtUtil completes without throwing an exception.
     */
    public boolean validateRefreshToken(String token){
        try {
            // Call JwtUtil to check token validity. This will now throw JwtException on failure.
            jwtUtil.validateRefreshToken(token);
            return true; // If no exception, token is valid
        } catch (JwtException jwtException) {
            // If any issue (expired, tampered, invalid signature), return false
            System.out.println("Refresh Token validation failed: " + jwtException.getMessage());
            return false;
        }
    }

}
