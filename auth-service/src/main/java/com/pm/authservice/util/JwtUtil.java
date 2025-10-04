package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretkey;

    // Inject secret key from application properties
    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        // Convert secret string to HMAC SHA key
        this.secretkey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Generate short-lived access token
    public String generateAccessToken(String email, String role){
        return Jwts.builder()
                .subject(email) // Subject = user email
                .claim("role", role) // Store user role
                .issuedAt(new Date()) // Issue time
                .expiration(new Date(System.currentTimeMillis() + 1000*60*30)) // 30 mins expiry
                .signWith(secretkey) // Sign token with secret key
                .compact();
    }

    // Generate long-lived refresh token
    public String generateRefreshToken(String email, String role){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*60*24*10)) // 10 days expiry
                .signWith(secretkey)
                .compact();
    }


    /**
     * Access Token validation
     * This method is updated to throw JwtException instead of returning false.
     * The calling service method will handle the exception.
     */
    public void validateAccessToken(String token) throws JwtException {
        // If the token is invalid (expired, wrong signature, malformed), parseSignedClaims will throw JwtException.
        Jwts.parser()
                .verifyWith((SecretKey) secretkey)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Refresh Token validation
     * This method is updated to throw JwtException instead of returning false.
     * The calling service method will handle the exception.
     */
    public void validateRefreshToken(String token) throws JwtException {
        // If the token is invalid (expired, wrong signature, malformed), parseSignedClaims will throw JwtException.
        Jwts.parser()
                .verifyWith((SecretKey) secretkey)
                .build()
                .parseSignedClaims(token);
    }
}
