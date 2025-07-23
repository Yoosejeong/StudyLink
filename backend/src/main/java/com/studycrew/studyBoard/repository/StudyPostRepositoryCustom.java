package com.studycrew.studyBoard.repository;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyPostRepositoryCustom {
    Page<GetStudyPostListResponse> searchByStatusAndNotDeleted(StudyStatus status, Pageable pageable);
}
