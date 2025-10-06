package com.pm.patientservice.mapper;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.model.Patient;

// Mapper class to convert between Patient entity and DTOs
public class PatientMapper {

    // Convert Patient entity to PatientResponseDTO (for sending to client)
    public static PatientResponseDTO toDTO(Patient patient){
        PatientResponseDTO dto = new PatientResponseDTO();

        dto.setId(patient.getId().toString());          
        dto.setName(patient.getName());
        dto.setAddress(patient.getAddress());
        dto.setEmail(patient.getEmail());
        dto.setDateOfBirth(patient.getDateOfBirth().toString()); 

        return dto;
    }

    // Convert PatientRequestDTO (from client) to Patient entity (for DB)
    public static Patient toModel(PatientRequestDTO dto){
        Patient patient = new Patient();

        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setAddress(dto.getAddress());
        patient.setDateOfBirth(dto.getDateOfBirth());     
        patient.setRegisteredDate(dto.getRegisteredDate());

        return patient;
    }
}
