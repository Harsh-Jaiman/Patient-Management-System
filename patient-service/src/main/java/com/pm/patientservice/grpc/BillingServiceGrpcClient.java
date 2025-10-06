package com.pm.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Spring service for calling external Billing Service via gRPC
@Service
public class BillingServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);

    // gRPC stub used to make synchronous calls to the BillingService
    private final BillingServiceGrpc.BillingServiceBlockingStub stub;

    
    /**
     * Constructor initializes the gRPC client and connects to Billing Service
     * @param serverAddress - hostname of Billing Service (default localhost)
     * @param serverPort - port of Billing Service gRPC server (default 9001)
     */
    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort
    ){
        
        log.info("Connecting to the Billing Service GRPC service at {}:{}", serverAddress, serverPort);

        // Create a channel to communicate with the gRPC server
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverAddress, serverPort)
                .usePlaintext() // Disable TLS for local development
                .build();

        stub = BillingServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Calls the BillingService gRPC method "createBillingRequest"
     * @param id - Patient ID
     * @param name - Patient Name
     * @param email - Patient Email
     * @return BillingResponse from Billing Service
     */
    public BillingResponse createBillingAccount(String id, String name, String email){

        // Build gRPC request object from patient data
        BillingRequest request = BillingRequest.newBuilder()
                .setPatientId(id)
                .setName(name)
                .setEmail(email)
                .build();

        // Make synchronous RPC call to Billing Service
        BillingResponse response = stub.createBillingRequest(request);

        // Log the response for debugging
        log.info("Received response from billing service via grpc: {}", response);
        return response; // Return response to caller
    }
}
