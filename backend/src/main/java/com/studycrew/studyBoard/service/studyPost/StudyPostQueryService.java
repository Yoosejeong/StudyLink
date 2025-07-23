package com.studycrew.studyBoard.service.studyPost;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyPostQueryService {
    StudyPost getStudyPost(Long studyPostId);
    Page<GetStudyPostListResponse> getStudyPostList(StudyStatus status, Pageable pageable);
}
