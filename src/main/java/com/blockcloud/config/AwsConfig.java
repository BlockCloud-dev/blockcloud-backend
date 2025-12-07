package com.blockcloud.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;

/**
 * AWS 설정 클래스
 * AWS Cost Explorer API 클라이언트를 설정합니다.
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "aws")
@Getter
@Setter
public class AwsConfig {

    private Credentials credentials;
    private String region;

    @Getter
    @Setter
    public static class Credentials {
        private String accessKeyId;
        private String secretAccessKey;
    }

    @Bean
    public CostExplorerClient costExplorerClient() {
        log.info("Initializing AWS Cost Explorer Client...");
        
        try {
            // AWS 자격 증명 검증
            if (credentials == null) {
                log.error("AWS credentials configuration is missing");
                throw new IllegalArgumentException("AWS credentials configuration is missing");
            }
            
            String accessKeyId = credentials.getAccessKeyId();
            String secretAccessKey = credentials.getSecretAccessKey();
            
            log.info("AWS Access Key ID: {}", accessKeyId != null ? accessKeyId.substring(0, Math.min(8, accessKeyId.length())) + "..." : "null");
            
            if (accessKeyId == null || accessKeyId.trim().isEmpty()) {
                log.error("AWS Access Key ID is missing or empty");
                throw new IllegalArgumentException("AWS Access Key ID is missing or empty");
            }
            
            if (secretAccessKey == null || secretAccessKey.trim().isEmpty()) {
                log.error("AWS Secret Access Key is missing or empty");
                throw new IllegalArgumentException("AWS Secret Access Key is missing or empty");
            }
            
            // 기본값 체크 (플레이스홀더 값들)
            if ("your-access-key".equals(accessKeyId) || 
                "your-secret-key".equals(secretAccessKey) ||
                "AKIAXXXXXXXXXXXXXXXX".equals(accessKeyId) ||
                "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX".equals(secretAccessKey)) {
                log.error("Please configure valid AWS credentials in application.yml");
                throw new IllegalArgumentException("Please configure valid AWS credentials in application.yml");
            }

            log.info("Creating AWS Basic Credentials...");
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(
                    accessKeyId.trim(),
                    secretAccessKey.trim()
            );

            log.info("Building Cost Explorer Client...");
            CostExplorerClient client = CostExplorerClient.builder()
                    .region(Region.US_EAST_1) // Cost Explorer는 us-east-1만 지원
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .build();
            
            log.info("AWS Cost Explorer Client initialized successfully");
            return client;
            
        } catch (Exception e) {
            log.error("Failed to initialize AWS Cost Explorer Client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize AWS Cost Explorer Client", e);
        }
    }
}
