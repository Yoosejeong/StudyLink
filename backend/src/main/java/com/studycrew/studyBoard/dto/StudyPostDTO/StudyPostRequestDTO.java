package com.studycrew.studyBoard.dto.StudyPostDTO;

import com.studycrew.studyBoard.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class StudyPostRequestDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class StudyPostCreate{
        private String title;
        private String content;
        private Category category;
        private int maxPeople;
        private List<String> tags;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class StudyPostRequestUpdate{
        private String title;
        private String content;
    }
}
