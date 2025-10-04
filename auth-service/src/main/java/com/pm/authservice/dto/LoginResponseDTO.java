package com.pm.authservice.dto;

public class LoginResponseDTO {

    private final String accessToken; // Short-lived JWT
    private final String refreshToken; // Long-lived JWT

    public LoginResponseDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getter for access token
    public String getAccessToken() { return accessToken; }

    // Getter for refresh token
    public String getRefreshToken() { return refreshToken; }
}
