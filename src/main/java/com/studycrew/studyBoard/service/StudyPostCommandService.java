package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;

public interface StudyPostCommandService {
    StudyPost createStudyPost(StudyPostCreate dto, User user);

    void deleteStudyPost(Long studyPostId, User user);
}
