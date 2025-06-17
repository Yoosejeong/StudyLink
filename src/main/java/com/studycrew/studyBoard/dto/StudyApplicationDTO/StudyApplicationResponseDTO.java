package com.studycrew.studyBoard.dto.StudyApplicationDTO;

import com.studycrew.studyBoard.enums.ApplicationStatus;
import com.studycrew.studyBoard.enums.StudyStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StudyApplicationResponseDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class StudyApplicationResult{
        private Long studyPostId;
        private Long studyApplicationId;
        private ApplicationStatus applicationStatus;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudyApplicationListResponse {
        private Long studyApplicationId;
        private Long userId;
        private String nickname;
        private ApplicationStatus applicationStatus;
        private LocalDateTime appliedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyStudyApplicationResponse {
        private Long studyApplicationId;
        private Long studyPostId;
        private String studyTitle;
        private ApplicationStatus applicationStatus;
        private LocalDateTime appliedAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudyApplicationApproveResponse {
        private Long studyApplicationId;
        private ApplicationStatus applicationStatus;
        private int acceptedPeople;
    }
}
