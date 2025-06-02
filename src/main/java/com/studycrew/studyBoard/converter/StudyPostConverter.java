package com.studycrew.studyBoard.converter;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.StudyStatus;

public class StudyPostConverter {

    public static StudyPost toStudyPost(StudyPostCreate dto){
        return StudyPost.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .maxPeople(dto.getMax_people())
                .currentPeople(dto.getCurrent_people())
                .studyStatus(StudyStatus.RECRUITING)
                .build();
    }

    public static StudyPostResponseDTO.GetStudyPost toGetStudyPost(StudyPost studyPost){
        return StudyPostResponseDTO.GetStudyPost.builder()
                .studyPostId(studyPost.getId())
                .title(studyPost.getTitle())
                .content(studyPost.getContent())
                .max_people(studyPost.getMaxPeople())
                .current_people(studyPost.getCurrentPeople())
                .studyStatus(studyPost.getStudyStatus())
                .build();
    }
}
