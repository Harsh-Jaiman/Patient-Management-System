package com.pm.stack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.servicediscovery.PrivateDnsNamespace;
import software.amazon.awscdk.services.servicediscovery.*;
import software.amazon.awscdk.services.servicediscovery.INamespace;

public class AwsDeployment extends Stack {

    private final Vpc vpc;
    private final Cluster ecsCluster;
    private final PrivateDnsNamespace privateDnsNamespace; // DNS namespace (pm.local)

    // Update these ECR image URIs if your account/region differ
    private static final String ECR_BASE = "867344477207.dkr.ecr.eu-north-1.amazonaws.com";
    private static final String AUTH_IMAGE    = ECR_BASE + "/auth-service:latest";
    private static final String BILLING_IMAGE = ECR_BASE + "/billing-service:latest";
    private static final String PATIENT_IMAGE = ECR_BASE + "/patient-service:latest";
    private static final String ANALYTICS_IMAGE = ECR_BASE + "/analytics-service:latest";
    private static final String API_GATEWAY_IMAGE = ECR_BASE + "/api-gateway:latest";

    public AwsDeployment(final App scope, final String id, final StackProps props){
        super(scope, id, props);

        // 1️⃣ Create VPC with 2 AZs for HA
        this.vpc = Vpc.Builder.create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();

        // 2️⃣ Create ECS Cluster
        this.ecsCluster = Cluster.Builder.create(this, "PatientManagementCluster")
                .vpc(vpc)
                .build();

        // 3️⃣ Create Private DNS Namespace for service discovery (pm.local)
        this.privateDnsNamespace = PrivateDnsNamespace.Builder.create(this, "PatientManagementNamespace")
                .name("pm.local")
                .vpc(vpc)
                .build();

        // 4️⃣ Create Fargate Services using ECR images and service discovery

        // Auth Service
        FargateService authService = createFargateService(
                "AuthService",
                "auth-service",
                AUTH_IMAGE,
                List.of(4005),
                Map.of(
                        "SPRING_DATASOURCE_PASSWORD", "password",
                        "SPRING_DATASOURCE_URL", "jdbc:postgresql://auth-service-db:5432/db",
                        "SPRING_DATASOURCE_USERNAME", "admin_user",
                        "SPRING_JPA_HIBERNATE_DDL_AUTO", "update",
                        "SPRING_SQL_INIT_MODE", "always",
                        "JWT_SECRET", "9f8a!Dk3@pXyQ7zR1#LmWt6Vb2$GhJ0k"
                )
        );

        // Billing Service
        FargateService billingService = createFargateService(
                "BillingService",
                "billing-service",
                BILLING_IMAGE,
                List.of(4001, 9001),
                null
        );

        // Analytics Service
        FargateService analyticsService = createFargateService(
                "AnalyticsService",
                "analytics-service",
                ANALYTICS_IMAGE,
                List.of(4002),
                Map.of("SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
        );

        // Patient Service
        FargateService patientService = createFargateService(
                "PatientService",
                "patient-service",
                PATIENT_IMAGE,
                List.of(4000),
                Map.of(
                        "BILLING_SERVICE_ADDRESS", "http://billing-service.pm.local:4001",
                        "BILLING_SERVICE_GRPC_PORT", "9001",
                        "AUTH_SERVICE_ADDRESS", "http://auth-service.pm.local:4005",
                        "SPRING_DATASOURCE_PASSWORD", "password",
                        "SPRING_DATASOURCE_URL", "jdbc:postgresql://patient-service-db:5432/db",
                        "SPRING_DATASOURCE_USERNAME", "admin_user",
                        "SPRING_JPA_HIBERNATE_DDL_AUTO", "update",
                        "SPRING_KAFKA_BOOTSTRAP_SERVERS", "kafka:9092",
                        "SPRING_SQL_INIT_MODE", "always"
                )
        );

        // Task/service ordering (optional)
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(authService);

        // API Gateway (ALB + Fargate)
        createApiGatewayService();
    }

    private FargateService createFargateService(String id,
                                                String serviceName,
                                                String imageUri,
                                                List<Integer> ports,
                                                Map<String, String> additionalEnvVars) {

        // Task Definition
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, id + "Task")
                .cpu(512)
                .memoryLimitMiB(1024)
                .build();

        // Environment Variables
        Map<String, String> envVars = new HashMap<>();
        if(additionalEnvVars != null){
            envVars.putAll(additionalEnvVars);
        }

        // Container Definition
        ContainerDefinitionOptions containerOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageUri))
                .portMappings(ports.stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                .logGroupName("/ecs/" + serviceName)
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_WEEK)
                                .build())
                        .streamPrefix(serviceName)
                        .build()))
                .environment(envVars)
                .build();

        taskDefinition.addContainer(serviceName + "Container", containerOptions);

        // Fargate Service with CloudMap Service Discovery
        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false) // internal service
                .serviceName(serviceName)
                .cloudMapOptions(CloudMapOptions.builder()
                        .cloudMapNamespace((INamespace) privateDnsNamespace)
                        .name(serviceName)
                        .build())
                .build();
    }

    private void createApiGatewayService() {
        // ALB + Fargate Service for API Gateway
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "APIGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        // Auth service DNS for env injection
        String authServiceDns = "http://auth-service.pm.local:4005";

        ContainerDefinitionOptions containerOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(API_GATEWAY_IMAGE))
                .environment(Map.of(
                        "SPRING_PROFILES_ACTIVE", "prod",
                        "AUTH_SERVICE_URL", authServiceDns
                ))
                .portMappings(List.of(4004).stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port)
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                .logGroupName("/ecs/api-gateway")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_WEEK)
                                .build())
                        .streamPrefix("api-gateway")
                        .build()))
                .build();

        taskDefinition.addContainer("APIGatewayContainer", containerOptions);

        ApplicationLoadBalancedFargateService.Builder.create(this, "APIGatewayService")
                .cluster(ecsCluster)
                .serviceName("api-gateway")
                .taskDefinition(taskDefinition)
                .desiredCount(1)
                .publicLoadBalancer(true) // exposes ALB publicly
                .healthCheckGracePeriod(Duration.seconds(60))
                .build();
    }

    public static void main(final String[] args) {
        App app = new App();

        // Option: set explicit account/region or rely on CDK env variables
        StackProps props = StackProps.builder()
                .env(Environment.builder()
                        .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                        .region(System.getenv("CDK_DEFAULT_REGION"))
                        .build())
                .build();

        new AwsDeployment(app, "PatientManagementStack", props);
        app.synth();

        System.out.println("AWS CDK stack synthesized successfully!");
    }
}
