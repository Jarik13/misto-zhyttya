package org.example.mediaservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {
    @Bean
    public S3Client s3Client(
            @Value("${aws.s3.access-key}")
            String accessKey,

            @Value("${aws.s3.secret-key}")
            String secretKey
    ) {
        AwsBasicCredentials credentials = AwsBasicCredentials.builder()
                .accessKeyId(accessKey)
                .secretAccessKey(secretKey)
                .build();

        return S3Client.builder()
                .credentialsProvider(() -> credentials)
                .region(Region.EU_NORTH_1)
                .build();
    }
}
