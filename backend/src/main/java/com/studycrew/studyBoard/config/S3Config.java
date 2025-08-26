package com.studycrew.studyBoard.config;

import com.studycrew.studyBoard.config.props.PresignProps;
import com.studycrew.studyBoard.config.props.S3Props;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties({S3Props.class, PresignProps.class})
public class S3Config {

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider,
                                   AwsRegionProvider regionProvider) {
        return S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(regionProvider.getRegion())
                .build();
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider,
                             AwsRegionProvider regionProvider) {
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(regionProvider.getRegion())
                .build();
    }
}
