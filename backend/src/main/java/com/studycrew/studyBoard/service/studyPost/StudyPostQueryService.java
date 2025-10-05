package com.studycrew.studyBoard.service.studyPost;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.*;

public interface StudyPostQueryService {
    GetStudyPostAndProfile getStudyPost(Long studyPostId);
    Page<GetStudyPostListResponse> getStudyPostList(String rawKeyword, StudyStatus status, Pageable pageable);
}
