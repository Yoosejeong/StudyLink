package com.studycrew.studyBoard.repository;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyPostRepositoryCustom {
    List<GetStudyPostListResponse> searchByStatusAndNotDeleted(String rawKeyword, StudyStatus status, Pageable pageable);
    long countByStatusAndNotDeleted(String rawKeyword, StudyStatus status);
}
