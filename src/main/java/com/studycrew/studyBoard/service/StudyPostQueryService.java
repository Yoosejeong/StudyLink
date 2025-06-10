package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPost;
import com.studycrew.studyBoard.entity.StudyPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyPostQueryService {
    StudyPost getStudyPost(Long studyPostId);
    Page<StudyPostResponseDTO.GetStudyPostListResponse> getStudyPostList(Pageable pageable);
}
