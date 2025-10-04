package com.pm.patientservice.repository;

// Import entity
import com.pm.patientservice.model.Patient;

// Spring Data JPA repository
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

// Marks this as a Spring repository bean
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    // Checks if a patient exists with the given email
    // Used to prevent duplicate emails when creating a patient
    boolean existsByEmail(String id);

    // Checks if a patient exists with the given email but excluding a specific patient ID
    // Used when updating a patient to prevent email conflicts with other patients
    boolean existsByEmailAndIdNot(String email, UUID id);
}
