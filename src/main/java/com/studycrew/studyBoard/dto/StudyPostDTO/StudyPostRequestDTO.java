package com.studycrew.studyBoard.dto.StudyPostDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class StudyPostRequestDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class StudyPostCreate{
        private String title;
        private String content;
        private int max_people;
        private int current_people;
    }
}
