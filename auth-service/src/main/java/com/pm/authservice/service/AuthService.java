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

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates user credentials and generates access & refresh tokens if valid.
     */
    public Optional<LoginResponseDTO> authenticate(LoginRequestDTO loginRequestDTO) {
        return userService.findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
                .map(u -> {
                    String accessToken = jwtUtil.generateAccessToken(u.getEmail(), u.getRole());
                    String refreshToken = jwtUtil.generateRefreshToken(u.getEmail(), u.getRole());
                    return new LoginResponseDTO(accessToken, refreshToken);
                });
    }


    /**
     * Registers a new user if email is not already taken, and generates tokens.
     */
    public Optional<LoginResponseDTO> signup(LoginRequestDTO signupRequestDTO) {
        if (userService.findByEmail(signupRequestDTO.getEmail()).isPresent()) {
            return Optional.empty(); // User already exists
        }
        // Create new user with encoded password
        userService.createUser(signupRequestDTO.getEmail(),
                passwordEncoder.encode(signupRequestDTO.getPassword()), "USER");

        // Authenticate the newly created user to generate tokens
        String accessToken = jwtUtil.generateAccessToken(signupRequestDTO.getEmail(), "USER");
        String refreshToken = jwtUtil.generateRefreshToken(signupRequestDTO.getEmail(), "USER");

        return  Optional.of(new LoginResponseDTO(accessToken, refreshToken));
    }

    /**
     * Validates the access token using JwtUtil. Returns true if valid, false otherwise.
     */
    public boolean validateAccessToken(String token){
        try {
            jwtUtil.validateAccessToken(token);
            return true;
        } catch (JwtException jwtException) {
            System.out.println("Access Token validation failed: " + jwtException.getMessage());
            return false;
        }
    }

    /**
     * Validates the refresh token using JwtUtil. Returns true if valid, false otherwise.
     */
    public boolean validateRefreshToken(String token){
        try {
            jwtUtil.validateRefreshToken(token);
            return true;
        } catch (JwtException jwtException) {
            System.out.println("Refresh Token validation failed: " + jwtException.getMessage());
            return false;
        }
    }

}
