package com.studycrew.studyBoard.dto.S3DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

public class S3ResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PresignPutResponse {
        private String method;
        private String url;
        private Map<String, String> headers;
        private String key;
        private Instant expiresAt;
    }
}
