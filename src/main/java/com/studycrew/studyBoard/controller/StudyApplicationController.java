package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.StudyApplicationConverter;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationListResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationResult;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.service.studyApplication.StudyApplicationCommandService;
import com.studycrew.studyBoard.service.studyApplication.StudyApplicationQueryService;
import com.studycrew.studyBoard.service.user.UserQueryService;
import com.sun.net.httpserver.Authenticator.Success;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyApplicationController {

    private final StudyApplicationCommandService studyApplicationCommandService;
    private final UserQueryService userQueryService;
    private final StudyApplicationQueryService studyApplicationQueryService;

    @PostMapping("/api/study-posts/{studyPostId}/applications")
    public ApiResponse<StudyApplicationResult> applyStudyPost(@PathVariable("studyPostId") Long studyPostId,  @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        StudyApplication studyApplication = studyApplicationCommandService.applyStudyApplication(studyPostId, user);
        StudyApplicationResult responseDTO = StudyApplicationConverter.toStudyApplicationResult(studyApplication);
        return ApiResponse.of(SuccessStatus._STUDY_APPLICATION_CREATED,responseDTO);
    }

    @GetMapping("/api/study-posts/{studyPostId}/applications")
    public ApiResponse<List<StudyApplicationListResponse>> getAllStudyApplication(@PathVariable("studyPostId") Long studyPostId) {
        List<StudyApplicationListResponse> list = studyApplicationQueryService.findAllApplicants(studyPostId);
        return ApiResponse.of(SuccessStatus._STUDY_APPLICANT_LIST_RETRIEVED, list);
    }
}
