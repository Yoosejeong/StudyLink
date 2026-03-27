package com.studycrew.studyBoard.dto.StudyPostDTO;

import com.querydsl.core.annotations.QueryProjection;
import com.studycrew.studyBoard.enums.Category;
import com.studycrew.studyBoard.enums.StudyStatus;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StudyPostResponseDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class GetStudyPost{
        private Long studyPostId;
        private Long userId;
        private String title;
        private String nickname;
        private String content;
        private int maxPeople;
        private int acceptedPeople;
        private StudyStatus studyStatus;
        private Category category;
        private List<String> tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class GetStudyPostAndProfile{
        private Long studyPostId;
        private Long userId;
        private String title;
        private String nickname;
        private String content;
        private int maxPeople;
        private int acceptedPeople;
        private StudyStatus studyStatus;
        private Category category;
        private List<String> tags;
        private String profileUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }


    //@Builder
    @NoArgsConstructor
    @Getter
    public static class GetStudyPostListResponse{
        private Long studyPostId;
        private String title;
        private String nickname;
        private int maxPeople;
        private int acceptedPeople;
        private StudyStatus studyStatus;
        private Category category;
        private String profileKey;
        private String profileUrl;
        private List<String> tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @QueryProjection
        public GetStudyPostListResponse(Long studyPostId, String title, String nickname, int maxPeople,
                                        int acceptedPeople, StudyStatus studyStatus, Category category, String profileKey, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.studyPostId = studyPostId;
            this.title = title;
            this.nickname = nickname;
            this.maxPeople = maxPeople;
            this.acceptedPeople = acceptedPeople;
            this.studyStatus = studyStatus;
            this.category = category;
            this.profileKey = profileKey;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public void attachTags(List<String> tags) {
            this.tags = tags;
        }

        public void setProfileUrl(String url){
            this.profileUrl = url;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StudyPostCursorResponse {
        private List<GetStudyPostListResponse> items;
        private boolean hasNext;
        private LocalDateTime nextCursorCreatedAt;
        private Long nextCursorId;
    }

}
