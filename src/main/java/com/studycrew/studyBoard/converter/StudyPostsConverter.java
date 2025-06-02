package com.studycrew.studyBoard.converter;

import com.studycrew.studyBoard.dto.StudyPostsDTO.StudyPostsRequestDTO.StudyPostsCreate;
import com.studycrew.studyBoard.dto.StudyPostsDTO.StudyPostsResponseDTO;
import com.studycrew.studyBoard.entity.StudyPosts;
import com.studycrew.studyBoard.enums.StudyStatus;

public class StudyPostsConverter {

    public static StudyPosts toStudyPosts(StudyPostsCreate dto){
        return StudyPosts.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .maxPeople(dto.getMax_people())
                .currentPeople(dto.getCurrent_people())
                .studyStatus(StudyStatus.RECRUITING)
                .build();
    }

    public static StudyPostsResponseDTO.GetStudyPosts toGetStudyPosts(StudyPosts studyPosts){
        return StudyPostsResponseDTO.GetStudyPosts.builder()
                .studyPostsId(studyPosts.getId())
                .title(studyPosts.getTitle())
                .content(studyPosts.getContent())
                .max_people(studyPosts.getMaxPeople())
                .current_people(studyPosts.getCurrentPeople())
                .studyStatus(studyPosts.getStudyStatus())
                .build();
    }
}
