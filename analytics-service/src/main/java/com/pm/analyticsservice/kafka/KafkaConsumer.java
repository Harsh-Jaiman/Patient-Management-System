package com.pm.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service // Marks this class as a Spring service component
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class); // Logger for this class

    @KafkaListener(
            topics = "patient",
            groupId = "analytics-service",
            containerFactory = "kafkaListenerContainerFactory" 
    )
    public void consumerEvent(
            @Payload byte[] event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, 
            @Header(KafkaHeaders.OFFSET) long offset
    ){
        try {
            // Deserialize the byte array to a PatientEvent object using Protobuf
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

    
            log.info("Received patient event: [PatientId={}, PatientName={}, PatientEmail={}] from partition={}, offset={}",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail(),
                    partition,
                    offset
            );

        } catch (InvalidProtocolBufferException e) {
            // Log an error if deserialization fails
            log.error("Error de-serializing event: {}", e.getMessage(), e);
        }
    }
}
