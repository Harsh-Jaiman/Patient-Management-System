package com.pm.authservice.dto;

import jakarta.validation.constraints.*;

public class LoginRequestDTO {

    @NotBlank(message="Email is required")
    @Email(message="Email should be valid")
    private String email; // Email from login request

    @NotBlank(message="Password is required")
    @Size(min = 8, message="Password must be atleast 8 characters long")
    private String password; // Password from login request

    // Getters & Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
