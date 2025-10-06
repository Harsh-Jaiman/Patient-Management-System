package com.pm.authservice.dto;

public class LoginResponseDTO {

    private final String accessToken;
    private final String refreshToken;

    public LoginResponseDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    
    public String getAccessToken() { return accessToken; }

    
    public String getRefreshToken() { return refreshToken; }
}
