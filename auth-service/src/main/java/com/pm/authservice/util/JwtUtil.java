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

    /**
     * Initializes JwtUtil with secret key from application properties.
     */
    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        this.secretkey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a short-lived access token (30 minutes expiry).
     */
    public String generateAccessToken(String email, String role){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*30))
                .signWith(secretkey)
                .compact();
    }

    /**
     * Generates a long-lived refresh token (10 days expiry).
     */
    public String generateRefreshToken(String email, String role){
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000*60*60*24*10))
                .signWith(secretkey)
                .compact();
    }

    /**
     * Validates the access token. Throws JwtException if invalid.
     */
    public void validateAccessToken(String token) throws JwtException {
        Jwts.parser()
                .verifyWith((SecretKey) secretkey)
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Validates the refresh token. Throws JwtException if invalid.
     */
    public void validateRefreshToken(String token) throws JwtException {
        Jwts.parser()
                .verifyWith((SecretKey) secretkey)
                .build()
                .parseSignedClaims(token);
    }
}
