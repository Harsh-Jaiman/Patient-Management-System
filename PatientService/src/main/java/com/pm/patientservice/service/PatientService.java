package com.pm.patientservice.service;

// Import DTOs for request and response
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;

// Import custom exceptions
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;

// gRPC client for billing service
import com.pm.patientservice.grpc.BillingServiceGrpcClient;

// Kafka producer to send patient events
import com.pm.patientservice.kafka.KafkaProducer;

// Mapper to convert between DTO and entity
import com.pm.patientservice.mapper.PatientMapper;

// Patient entity
import com.pm.patientservice.model.Patient;

// Repository to access database
import com.pm.patientservice.repository.PatientRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Static import to call mapper methods directly
import static com.pm.patientservice.mapper.PatientMapper.toDTO;

// Marks this class as a Spring Service (business logic layer)
@Service
public class PatientService {

    // Repository to access patient data
    private final PatientRepository patientRepository;

    // gRPC client to communicate with billing service
    private final BillingServiceGrpcClient billingServiceGrpcClient;

    // Kafka producer to send events
    private final KafkaProducer kafkaProducer;

    // Constructor injection for repository, gRPC client, and Kafka producer
    public PatientService(PatientRepository patientRepository,
                          BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
    }

    // Fetch all patients from DB
    public List<PatientResponseDTO> getPatients(){
        // Retrieve all Patient entities
        List<Patient> patients = patientRepository.findAll();

        // Convert each Patient entity to PatientResponseDTO using mapper
        List<PatientResponseDTO>  patientResponseDTOList = patients.stream()
                .map(PatientMapper::toDTO).toList();

        // Return list of DTOs
        return patientResponseDTOList;
    }

    // Create a new patient
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        // Check if email already exists in DB to prevent duplicates
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            // Throw custom exception if duplicate email found
            throw new EmailAlreadyExistsException("A patient with this email"+" already exist "+patientRequestDTO.getAddress());
        }

        // Convert DTO to Patient entity and save to DB
        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        // Call billing service via gRPC to create billing account for this patient
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),
                newPatient.getName(), newPatient.getEmail());

        // Send a Kafka event to notify other services about new patient creation
        kafkaProducer.sendEvent(newPatient);

        // Convert saved entity to response DTO and return
        return  PatientMapper.toDTO(newPatient);
    }

    // Update an existing patient by UUID
    public PatientResponseDTO updatePatient(UUID id,
                                            PatientRequestDTO patientRequestDTO)
            throws PatientNotFoundException {
        // Look up the patient by ID, throw exception if not found
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: "+id));

        // Check if new email is already used by another patient
        if (!patient.getEmail().equals(patientRequestDTO.getEmail()) &&
                patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            // Throw exception for duplicate email
            throw new EmailAlreadyExistsException(
                    "A patient with this email"+" already exist "
                            +patientRequestDTO.getAddress());
        }

        // Update the properties of the found patient entity
        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth().toString()));

        // Save updated patient to database
        Patient updatedPatient = patientRepository.save(patient);

        // Convert updated entity to DTO and return
        return PatientMapper.toDTO(updatedPatient);
    }

    // Delete a patient by UUID
    public void deletePatient(UUID id) throws PatientNotFoundException {
        // Check if patient exists in DB
        Patient deletingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: "+id));

        // Delete the patient from the database
        patientRepository.deleteById(id);
    }
}
