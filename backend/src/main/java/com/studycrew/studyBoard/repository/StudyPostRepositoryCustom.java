package com.studycrew.studyBoard.repository;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.StudyPostCursorResponse;
import com.studycrew.studyBoard.enums.StudyStatus;

import java.time.LocalDateTime;

public interface StudyPostRepositoryCustom {
    StudyPostCursorResponse searchByStatusAndNotDeleted(String rawKeyword, StudyStatus status,
                                                        LocalDateTime lastCreatedAt, Long lastId, int size);
}
