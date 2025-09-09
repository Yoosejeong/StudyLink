package com.studycrew.studyBoard.converter;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.StudyStatus;

public class StudyPostConverter {

    public static StudyPost toStudyPost(StudyPostCreate dto, User user){
        return StudyPost.builder()
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .maxPeople(dto.getMaxPeople())
                .category(dto.getCategory())
                .studyStatus(StudyStatus.RECRUITING)
                .build();
    }

    public static StudyPostResponseDTO.GetStudyPost toGetStudyPost(StudyPost studyPost){
        return StudyPostResponseDTO.GetStudyPost.builder()
                .studyPostId(studyPost.getId())
                .userId(studyPost.getUser().getId())
                .nickname(studyPost.getUser().getNickname())
                .title(studyPost.getTitle())
                .content(studyPost.getContent())
                .maxPeople(studyPost.getMaxPeople())
                .acceptedPeople(studyPost.getAcceptedPeople())
                .studyStatus(studyPost.getStudyStatus())
                .category(studyPost.getCategory())
                .createdAt(studyPost.getCreatedAt())
                .updatedAt(studyPost.getUpdatedAt())
                .build();
    }

}
