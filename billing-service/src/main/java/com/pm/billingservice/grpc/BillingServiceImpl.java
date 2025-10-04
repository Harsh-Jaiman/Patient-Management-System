package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService // Marks this class as a gRPC server implementation
public class BillingServiceImpl extends BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);

    /**
     * This method is the implementation of the gRPC service defined in the proto file.
     * - It receives a BillingRequest from the client.
     * - It processes the request (business logic like DB operations, calculations, etc.).
     * - It sends back a BillingResponse to the client using responseObserver.
     */
    @Override
    public void createBillingRequest(BillingRequest request,
                                     StreamObserver<BillingResponse> responseObserver) {
        log.info("createBillingAccount request received {}", request.toString());

        // Example business logic: Here we are hardcoding a response.
        // In a real case, you would save data into DB and generate accountId dynamically.
        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345") // Hardcoded account ID
                .setStatus("ACTIVE")   // Hardcoded account status
                .build();

        // Send the response back to the client
        responseObserver.onNext(response);
        // Mark the RPC as completed
        responseObserver.onCompleted();
    }
}
