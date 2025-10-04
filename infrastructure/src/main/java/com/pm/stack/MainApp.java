package com.pm.stack;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class MainApp {
    public static void main(final String[] args) {
        App app = new App();

        // ✅ Hardcoded Account ID and Region (replace with yours)
        StackProps props = StackProps.builder()
                .env(Environment.builder()
                        .account("867344477207")   // 🔹 Your AWS Account ID
                        .region("eu-north-1")      // 🔹 Your AWS Region
                        .build())
                .build();

        // Instantiate the AWS deployment stack
        new AwsDeployment(app, "AwsDeploymentStack", props);

        app.synth();

        System.out.println("AWS CDK app synthesized successfully!");
    }
}
