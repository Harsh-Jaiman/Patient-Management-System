package com.pm.patientservice.exception;

// Custom runtime exception thrown when a patient email already exists in DB
public class EmailAlreadyExistsException extends RuntimeException {

    // Constructor accepts a message describing the error
    public EmailAlreadyExistsException(String message){
        super(message);
    }
}
