package com.pm.patientservice.mapper;

// Import DTOs
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;

// Import entity
import com.pm.patientservice.model.Patient;

// Mapper class to convert between Patient entity and DTOs
public class PatientMapper {

    // Convert Patient entity to PatientResponseDTO (for sending to client)
    public static PatientResponseDTO toDTO(Patient patient){
        PatientResponseDTO dto = new PatientResponseDTO();

        // Set fields from entity to DTO
        dto.setId(patient.getId().toString());          // UUID to String
        dto.setName(patient.getName());
        dto.setAddress(patient.getAddress());
        dto.setEmail(patient.getEmail());
        dto.setDateOfBirth(patient.getDateOfBirth().toString()); // LocalDate to String

        return dto; // Return the DTO
    }

    // Convert PatientRequestDTO (from client) to Patient entity (for DB)
    public static Patient toModel(PatientRequestDTO dto){
        Patient patient = new Patient();

        // Set fields from DTO to entity
        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setAddress(dto.getAddress());
        patient.setDateOfBirth(dto.getDateOfBirth());      // LocalDate
        patient.setRegisteredDate(dto.getRegisteredDate());// LocalDate

        return patient; // Return entity ready to save
    }
}
