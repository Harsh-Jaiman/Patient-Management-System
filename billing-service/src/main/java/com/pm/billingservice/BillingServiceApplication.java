package com.pm.billingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BillingServiceApplication {

    // Main entry point for the Billing Service application.
    // This will start the Spring Boot application and also run the gRPC server.
    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}
