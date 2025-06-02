package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.entity.StudyPost;

public interface StudyPostCommandService {
    StudyPost createStudyPost(StudyPostCreate dto);
}
