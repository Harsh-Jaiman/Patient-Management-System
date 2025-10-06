package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService 
public class BillingServiceImpl extends BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);

    /**
     * Handles createBillingRequest gRPC call.
     * Receives a BillingRequest, processes it, and sends a BillingResponse.
     */
    @Override
    public void createBillingRequest(BillingRequest request,
            StreamObserver<BillingResponse> responseObserver) {
        log.info("createBillingRequest request received {}", request);

        // Example business logic: Hardcoded response for demonstration.
        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
