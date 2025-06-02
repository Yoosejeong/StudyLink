package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.dto.StudyPostsDTO.StudyPostsRequestDTO.StudyPostsCreate;
import com.studycrew.studyBoard.entity.StudyPosts;

public interface StudyPostsCommandService {
    StudyPosts createStudyPosts(StudyPostsCreate dto);
}
