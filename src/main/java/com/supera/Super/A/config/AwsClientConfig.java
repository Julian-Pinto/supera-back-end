package com.supera.Super.A.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsClientConfig {

    private final AwsS3Config awsS3Config;

    public AwsClientConfig(AwsS3Config awsS3Config) {
        this.awsS3Config = awsS3Config;
    }

    private AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsS3Config.getRegion()))
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }

    @Bean
    public CloudFrontClient cloudFrontClient() {
        return CloudFrontClient.builder()
                .region(Region.of(awsS3Config.getRegion()))
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }
}
