package com.studycrew.studyBoard.service.studyApplication;

import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationListResponse;
import com.studycrew.studyBoard.repository.StudyApplicationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class StudyApplicationQueryServiceImpl implements StudyApplicationQueryService{

    private final StudyApplicationRepository studyApplicationRepository;

    @Override
    public List<StudyApplicationListResponse> findAllApplicants(Long studyPostId) {
        return List.of();
    }
}
