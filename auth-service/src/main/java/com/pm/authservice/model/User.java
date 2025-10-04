package com.pm.authservice.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;  // Unique ID for each user

    @Column(unique = true, nullable = false)
    private String email; // User email, must be unique

    @Column(nullable = false)
    private String password; // Hashed password

    @Column(nullable = false)
    private String role; // User role (e.g., ADMIN, USER)

    // Getters and Setters for all fields
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
