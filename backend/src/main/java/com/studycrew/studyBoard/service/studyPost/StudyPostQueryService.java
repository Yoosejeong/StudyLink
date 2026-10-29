package com.studycrew.studyBoard.service.studyPost;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostAndProfile;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.StudyPostCursorResponse;
import com.studycrew.studyBoard.enums.StudyStatus;

import java.time.LocalDateTime;

public interface StudyPostQueryService {
    GetStudyPostAndProfile getStudyPost(Long studyPostId);
    StudyPostCursorResponse getStudyPostList(String rawKeyword, StudyStatus status,
                                             LocalDateTime lastCreatedAt, Long lastId, int size);
}
