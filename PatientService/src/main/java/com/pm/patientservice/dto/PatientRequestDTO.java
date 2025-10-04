package com.pm.patientservice.dto;

// JSON date formatting
import com.fasterxml.jackson.annotation.JsonFormat;

// Validation group for creating a patient
import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;

// Validation annotations
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// DTO representing the data sent from client when creating/updating a patient
public class PatientRequestDTO {

    // Default constructor required for JSON deserialization
    public PatientRequestDTO() {}

    // Patient name cannot be blank and must be max 100 chars
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name can't exceed 100 characters")
    private String name;

    // Email must be non-empty and a valid format
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    // Address cannot be blank
    @NotBlank(message = "Address cannot be empty")
    private String address;

    // Date of birth cannot be null, serialized/deserialized in yyyy-MM-dd format
    @NotNull(message = "Date of birth cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    // Registered date is required only when creating a new patient (validation group)
    @NotNull(groups = CreatePatientValidationGroup.class, message = "Registered date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registeredDate;

    // Getter and setter for dateOfBirth
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // Getter and setter for name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Getter and setter for email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter for address
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    // Getter and setter for registeredDate
    public LocalDate getRegisteredDate() {
        return registeredDate;
    }
    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }
}
