package com.pm.patientservice.exception;

// Custom checked exception thrown when a patient is not found in DB
public class PatientNotFoundException extends Exception {

    // Constructor accepts a message describing the error
    public PatientNotFoundException(String message){
        super(message); // Pass message to base Exception class
    }
}
