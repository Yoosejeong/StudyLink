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

import com.studycrew.studyBoard.util.S3UrlUtil;
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
    private final S3UrlUtil s3UrlUtil;

    @Override
    public List<StudyApplicationListResponse> findAllApplicants(Long studyPostId) {
        List<ApplicationStatus> manageableStatuses = List.of(
                ApplicationStatus.PENDING,
                ApplicationStatus.ACCEPTED,
                ApplicationStatus.REJECTED
        );
        List<StudyApplication> studyApplicationList = studyApplicationRepository
                .findAllByStudyPostIdAndApplicationStatusIn(studyPostId, manageableStatuses);
        return studyApplicationList.stream()
                .map(app -> {
                    String key = app.getUser().getProfileKey();
                    String url = s3UrlUtil.buildPublicUrl(key);
                    return StudyApplicationConverter.toApplicationList(app, url);
                })
                .toList();
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
        Optional<StudyApplication> lastApplication = studyApplicationRepository.findTopByUserIdAndStudyPostIdOrderByIdDesc(
                userId, studyPostId);
        return HasAppliedResponse.builder()
                .studyApplicationId(lastApplication.map(StudyApplication::getId).orElse(null))
                .hasApplied(lastApplication.isPresent())
                .applicationStatus(lastApplication.map(StudyApplication::getApplicationStatus).orElse(null))
                .build();
    }
}
