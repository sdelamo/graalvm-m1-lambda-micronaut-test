package com.example;

import io.micronaut.aws.cdk.function.MicronautFunction;
import io.micronaut.aws.cdk.function.MicronautFunctionFile;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Tracing;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;
import java.util.HashMap;
import java.util.Map;

public class AppStack extends Stack {

    public AppStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public AppStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        Map<String, String> environmentVariables = new HashMap<>();
        Function function = MicronautFunction.create(ApplicationType.FUNCTION,
                true,
                this,
                "micronaut-function")
                .handler("com.example.FunctionRequestHandler")
                .environment(environmentVariables)
                .code(Code.fromAsset(functionPath()))
                .timeout(Duration.seconds(10))
                .memorySize(512)
                .tracing(Tracing.ACTIVE)
                .logRetention(RetentionDays.ONE_WEEK)
                .architecture(Architecture.ARM_64)
                .build();
        FunctionUrl functionUrl = function.addFunctionUrl(FunctionUrlOptions.builder()
                .authType(FunctionUrlAuthType.NONE)
                .build());
        CfnOutput.Builder.create(this, "MnTestApiUrl")
                .exportName("MnTestApiUrl")
                .value(functionUrl.getUrl())
                .build();
    }

    public static String functionPath() {
        return "../app/build/libs/" + functionFilename();
    }

    public static String functionFilename() {
        return MicronautFunctionFile.builder()
            .graalVMNative(true)
            .version("0.1")
            .archiveBaseName("app")
            .buildTool(BuildTool.GRADLE)
            .build();
    }
}