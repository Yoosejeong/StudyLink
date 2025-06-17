package com.studycrew.studyBoard.converter;

import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.MyStudyApplicationResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationApproveResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationListResponse;
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

    public static StudyApplicationListResponse toApplicationList(StudyApplication studyApplication){
        return StudyApplicationListResponse.builder()
                .applicationStatus(studyApplication.getApplicationStatus())
                .studyApplicationId(studyApplication.getId())
                .appliedAt(studyApplication.getCreatedAt())
                .userId(studyApplication.getUser().getId())
                .nickname(studyApplication.getUser().getNickname())
                .build();
    }

    public static MyStudyApplicationResponse toUserApplications(StudyApplication studyApplication) {
        return MyStudyApplicationResponse.builder()
                .applicationStatus(studyApplication.getApplicationStatus())
                .appliedAt(studyApplication.getCreatedAt())
                .studyPostId(studyApplication.getStudyPost().getId())
                .studyApplicationId(studyApplication.getId())
                .studyTitle(studyApplication.getStudyPost().getTitle())
                .build();
    }

    public static StudyApplicationApproveResponse toApproveResponse(StudyApplication studyApplication){
        return StudyApplicationApproveResponse.builder()
                .applicationStatus(studyApplication.getApplicationStatus())
                .acceptedPeople(studyApplication.getStudyPost().getAcceptedPeople())
                .studyApplicationId(studyApplication.getId())
                .build();
    }
}
