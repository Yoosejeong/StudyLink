package com.studycrew.studyBoard.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record S3Props(String bucket, String region) {
}
