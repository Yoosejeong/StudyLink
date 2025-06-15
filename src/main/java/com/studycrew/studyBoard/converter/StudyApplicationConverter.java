package com.studycrew.studyBoard.converter;

import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationResult;
import com.studycrew.studyBoard.entity.StudyApplication;

public class StudyApplicationConverter {
    public static StudyApplicationResult toStudyApplicationResult(StudyApplication studyApplication) {
        return StudyApplicationResult.builder()
                .studyApplicationId(studyApplication.getId())
                .studyPostId(studyApplication.getStudyPost().getId())
                .applicationStatus(studyApplication.getApplicationStatus())
                .build();
    }
}
