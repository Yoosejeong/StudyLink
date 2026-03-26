package com.studycrew.studyBoard.repository;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.StudyStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface StudyPostRepositoryCustom {
    Slice<GetStudyPostListResponse> searchByStatusAndNotDeleted(String rawKeyword, StudyStatus status, Pageable pageable);
}
