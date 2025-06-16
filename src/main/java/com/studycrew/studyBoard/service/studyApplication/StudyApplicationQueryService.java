package com.studycrew.studyBoard.service.studyApplication;

import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationListResponse;
import java.util.List;

public interface StudyApplicationQueryService {
    List<StudyApplicationListResponse> findAllApplicants(Long studyPostId);
}
