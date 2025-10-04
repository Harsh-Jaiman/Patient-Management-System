package com.pm.patientservice.dto;

// DTO representing data sent back to client after patient operations
public class PatientResponseDTO {

    // Patient ID as String (UUID)
    private String id;

    // Patient name
    private String name;

    // Patient email
    private String email;

    // Patient address
    private String address;

    // Date of birth as String (serialized)
    private String dateOfBirth;

    // Getter and setter for id
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    // Getter and setter for dateOfBirth
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
