package com.supera.Super.A.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
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
        if (awsS3Config.getAccessKey() != null && !awsS3Config.getAccessKey().isBlank()
                && awsS3Config.getSecretKey() != null && !awsS3Config.getSecretKey().isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsS3Config.getAccessKey().trim(), awsS3Config.getSecretKey().trim())
            );
        }

        if (awsS3Config.getProfile() != null && !awsS3Config.getProfile().isBlank()) {
            return ProfileCredentialsProvider.builder()
                    .profileName(awsS3Config.getProfile().trim())
                    .build();
        }

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
