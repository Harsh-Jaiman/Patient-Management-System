package com.pm.patientservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

// Handles exceptions globally across all controllers
@ControllerAdvice
public class GlobalExceptionHandler {

    // Logger for logging exceptions
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle validation errors from @Validated DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();

        // Extract field-specific validation errors and store in map
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        // Return 400 Bad Request with error details
        return ResponseEntity.badRequest().body(errors);
    }

    // Handle duplicate email exception
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExistsAlreadyException(
            EmailAlreadyExistsException emailAlreadyExistsException){

        // Log a warning about duplicate email
        log.warn("Email address already exist {}",
                emailAlreadyExistsException.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Email exists already");

        // Return 400 Bad Request with message
        return ResponseEntity.badRequest().body(errors);
    }

    // Handle patient not found exception
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePatientNotFoundException(
            PatientNotFoundException patientNotFoundException){

        // Log a warning about missing patient
        log.warn("Patient is not available in the database : {}",
                patientNotFoundException.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Patient not found");

        // Return 400 Bad Request with message
        return ResponseEntity.badRequest().body(errors);
    }
}
