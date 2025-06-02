package com.studycrew.studyBoard.dto.StudyPostsDTO;

import com.studycrew.studyBoard.enums.StudyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StudyPostsResponseDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class GetStudyPosts{
        private Long studyPostsId;
        private String title;
        private String content;
        private int max_people;
        private int current_people;
        private StudyStatus studyStatus;
    }
}
