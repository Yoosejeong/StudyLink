package com.studycrew.studyBoard.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.s3.presign")
public record PresignProps(Duration putTtl) {
}
