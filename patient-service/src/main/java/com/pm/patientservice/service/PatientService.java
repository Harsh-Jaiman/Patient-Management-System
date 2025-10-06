package com.pm.patientservice.service;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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

    public PatientService(PatientRepository patientRepository,
                          BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
    }

    /**
     * Fetch all patients from the database.
     **/
    public List<PatientResponseDTO> getPatients(){
        // Retrieve all Patient entities
        List<Patient> patients = patientRepository.findAll();

        // Convert each Patient entity to PatientResponseDTO using mapper
        List<PatientResponseDTO>  patientResponseDTOList = patients.stream()
                .map(PatientMapper::toDTO).toList();

        return patientResponseDTOList;
    }
    /**
     * Fetch a patient by UUID.
     * @throws PatientNotFoundException if patient is not found
     **/
    public PatientResponseDTO getPatientById(UUID id) throws PatientNotFoundException {
        // Retrieve the Patient entity by ID or throw exception if not found
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));

        return PatientMapper.toDTO(patient);
    }

    /**
     * Create a new patient, trigger billing account creation, and send Kafka event.
     * @throws EmailAlreadyExistsException if email already exists
     **/
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){

        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {

            throw new EmailAlreadyExistsException("A patient with this email already exists: " + patientRequestDTO.getAddress());
        
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        // Call billing service via gRPC to create billing account for this patient
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),newPatient.getName(), newPatient.getEmail());

        // Send a Kafka event to notify other services about new patient creation
        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDTO(newPatient);
    }

    /**
     * Update an existing patient by UUID.
     * @throws PatientNotFoundException if patient is not found
     * @throws EmailAlreadyExistsException if new email already exists
     **/
    public PatientResponseDTO updatePatient(UUID id,
                                            PatientRequestDTO patientRequestDTO)
            throws PatientNotFoundException {


        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));

        // Check if new email is already used by another patient
        if (!patient.getEmail().equals(patientRequestDTO.getEmail()) &&
                patientRepository.existsByEmail(patientRequestDTO.getEmail())) {

            throw new EmailAlreadyExistsException(
                    "A patient with this email already exists: " + patientRequestDTO.getAddress());
        }

        // Update the properties of the found patient entity
        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth().toString()));

        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toDTO(updatedPatient);
    }

    /**
     * Delete a patient by UUID.
     * @throws PatientNotFoundException if patient is not found
     */
    public void deletePatient(UUID id) throws PatientNotFoundException {

        Patient deletingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found with id: " + id));


        patientRepository.deleteById(id);
    }
}
