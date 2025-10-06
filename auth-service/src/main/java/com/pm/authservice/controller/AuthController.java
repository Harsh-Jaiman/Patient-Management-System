package com.pm.authservice.controller;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class AuthController {

    private final AuthService authService;
    private static final String INVALID_TOKEN_MESSAGE = "Token is invalid";

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user login and returns JWT tokens if authentication is successful.
     * Returns 401 if credentials are invalid, 500 for server errors.
     */
    @Operation(summary="Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        Optional<LoginResponseDTO> tokenOptional = Optional.empty();
        try {
            tokenOptional = authService.authenticate(loginRequestDTO);
            if (tokenOptional.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch(Exception ex) {
            System.out.println("Can't authenticate the user with email " + loginRequestDTO.getEmail());
            ex.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(tokenOptional.get());
    }
    

        /**
         * Handles user signup. Creates a new user if email is not taken, returns tokens.
         * Returns 409 if user exists, 500 for server errors.
         */
        @Operation(summary = "Register a new user and generate tokens")
        @PostMapping("/signup")
        public ResponseEntity<LoginResponseDTO> signup(@RequestBody LoginRequestDTO signupRequestDTO) {
            try {
                Optional<LoginResponseDTO> signupResult = authService.signup(signupRequestDTO);
                if (signupResult.isEmpty()) {
                    // User already exists
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
                return ResponseEntity.ok(signupResult.get());
            } catch (Exception ex) {
                System.out.println("Error during signup for email " + signupRequestDTO.getEmail());
                ex.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

    /**
     * Validates access token from Authorization header.
     * Returns 200 if valid, 401 if invalid or header is missing.
     */

    @Operation(summary = "Validate token")
    @GetMapping("/validate/access")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_TOKEN_MESSAGE);
        }
        String token = authHeader.substring(7);
        boolean isValid = authService.validateAccessToken(token);

        return isValid
                ? ResponseEntity.ok("Access Token validated")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_TOKEN_MESSAGE);
    }

    /**
     * Validates refresh token from Authorization header.
     * Returns 200 if valid, 401 if invalid or header is missing.
     */
    @Operation(summary = "Validate token")
    @GetMapping("/validate/refresh")
    public ResponseEntity<String> validateRefreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_TOKEN_MESSAGE);
        }
        String token = authHeader.substring(7);
        boolean isValid = authService.validateRefreshToken(token);

        return isValid
                ? ResponseEntity.ok("Refresh Token validated")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(INVALID_TOKEN_MESSAGE);
    }
}
