package com.pm.patientservice.kafka;

import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

// Spring service responsible for publishing patient events to Kafka
@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    // KafkaTemplate used to send messages to Kafka topics
    // Key = String (patientId), Value = byte[] (serialized protobuf event)
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a Patient event to Kafka
     * @param patient - the patient entity whose event needs to be published
     */
    public void sendEvent(Patient patient){

        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())  
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("Patient Created")           
                .build();

        try {
            // Send event to Kafka topic "patient"
            kafkaTemplate.send("patient", event.toByteArray());
            log.info("Patient-created event sent successfully: {}", event);

        } catch (Exception e) {
            log.info("Error sending patient-created event: {}", event);
        }
    }
}
