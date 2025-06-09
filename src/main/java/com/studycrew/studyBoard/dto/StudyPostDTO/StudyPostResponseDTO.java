package com.studycrew.studyBoard.dto.StudyPostDTO;

import com.studycrew.studyBoard.enums.StudyStatus;
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
        private String title;
        private String nickname;
        private String content;
        private int max_people;
        private int current_people;
        private StudyStatus studyStatus;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class StudyPostResponseUpdate{
        private Long studyPostId;
        private String title;
        private String content;
    }

}
