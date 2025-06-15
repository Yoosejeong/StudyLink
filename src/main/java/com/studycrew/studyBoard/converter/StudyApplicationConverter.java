package com.studycrew.studyBoard.converter;

import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationResult;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.ApplicationStatus;

public class StudyApplicationConverter {
    public static StudyApplicationResult toStudyApplicationResult(StudyApplication studyApplication) {
        return StudyApplicationResult.builder()
                .studyApplicationId(studyApplication.getId())
                .studyPostId(studyApplication.getStudyPost().getId())
                .applicationStatus(studyApplication.getApplicationStatus())
                .build();
    }

    public static StudyApplication toPendingApplication(StudyPost studyPost, User user){
        return StudyApplication.builder()
                .applicationStatus(ApplicationStatus.PENDING)
                .studyPost(studyPost)
                .user(user)
                .build();
    }
}
