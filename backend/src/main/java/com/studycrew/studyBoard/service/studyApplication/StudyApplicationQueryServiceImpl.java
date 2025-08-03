package com.studycrew.studyBoard.service.studyApplication;

import com.studycrew.studyBoard.converter.StudyApplicationConverter;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.MyStudyApplicationResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationListResponse;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import com.studycrew.studyBoard.repository.StudyApplicationRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;

import static com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.*;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class StudyApplicationQueryServiceImpl implements StudyApplicationQueryService{

    private final StudyApplicationRepository studyApplicationRepository;

    @Override
    public List<StudyApplicationListResponse> findAllApplicants(Long studyPostId) {
        List<StudyApplication> studyApplicationList = studyApplicationRepository.findByStudyPostId(studyPostId);
        return studyApplicationList.stream()
                .map(StudyApplicationConverter::toApplicationList)
                .collect(Collectors.toList());
    }

    @Override
    public List<MyStudyApplicationResponse> findMyStudyApplications(Long userId) {
        List<StudyApplication> userApplications = studyApplicationRepository.findByUserId(userId);
        return userApplications.stream()
                .map(StudyApplicationConverter::toUserApplications)
                .collect(Collectors.toList());
    }

    @Override
    public HasAppliedResponse hasUserApplied(Long userId, Long studyPostId) {
        Optional<ApplicationStatus> ApplicationStatus = studyApplicationRepository.findStatusByUserIdAndStudyPostId(
                userId, studyPostId);
        return HasAppliedResponse.builder()
                .hasApplied(ApplicationStatus.isPresent())
                .applicationStatus(ApplicationStatus.orElse(null))
                .build();
    }
}
