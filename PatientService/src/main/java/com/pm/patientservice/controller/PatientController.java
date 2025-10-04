package com.pm.patientservice.controller;

// Import DTOs for request and response
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;

// Validation group for creation
import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;

// Custom exception for patient not found
import com.pm.patientservice.exception.PatientNotFoundException;

// Service layer
import com.pm.patientservice.service.PatientService;

// Swagger/OpenAPI annotations for API documentation
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Validation support
import jakarta.validation.groups.Default;

// Spring imports for REST APIs
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Marks this class as a REST controller
@RestController
@RequestMapping("/patients")  // Base URL for patient endpoints: localhost:4000/patients
@Tag(name="Patient",description = "API for managing patients") // Swagger documentation
public class PatientController {

    // Inject PatientService via constructor
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // GET /patients -> returns list of all patients
    @GetMapping
    @Operation(summary = "Get all patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients(){
        // Call service layer to fetch all patients
        List<PatientResponseDTO> patients = patientService.getPatients();
        // Return 200 OK with list of patients
        return ResponseEntity.ok().body(patients);
    }

    // POST /patients -> create a new patient
    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(summary = "Create a new patient")
    public ResponseEntity<PatientResponseDTO> createPatient(
            // Validate request body using default and create-group validations
            @Validated({Default.class, CreatePatientValidationGroup.class})
            @RequestBody PatientRequestDTO patientRequestDTO) {

        // Call service layer to save patient in DB, trigger billing gRPC and Kafka events
        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);

        // Return the saved patient DTO as response
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    // PUT /patients/{id} -> update existing patient
    @PutMapping("/{id}")
    @Operation(summary = "Update a patient details")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable UUID id, // Path variable to identify patient
            @Validated({Default.class})
            @RequestBody PatientRequestDTO patientRequestDTO)
            throws PatientNotFoundException {

        // Call service layer to update patient, may throw exception if patient not found
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);

        // Return updated patient info
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    // DELETE /patients/{id} -> delete a patient
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable UUID id)
            throws PatientNotFoundException {

        // Call service layer to delete patient, throws exception if not found
        patientService.deletePatient(id);

        // Prepare a success message map
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Patient successfully deleted");

        // Return the response
        return ResponseEntity.ok(response);
    }
}
