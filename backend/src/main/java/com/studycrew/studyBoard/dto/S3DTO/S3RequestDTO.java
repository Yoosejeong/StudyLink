package com.studycrew.studyBoard.dto.S3DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class S3RequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PresignPutRequest {
        @NotBlank
        private String filename;
        @NotBlank
        private String contentType;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmProfileRequest {
        @NotBlank
        String newKey;
    }
}
