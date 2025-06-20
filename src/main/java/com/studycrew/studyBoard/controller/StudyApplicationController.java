package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.StudyApplicationConverter;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.MyStudyApplicationResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationApproveResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationListResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationRejectResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationResult;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.service.studyApplication.StudyApplicationCommandService;
import com.studycrew.studyBoard.service.studyApplication.StudyApplicationQueryService;
import com.studycrew.studyBoard.service.user.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 신청", description = "스터디 신청 관련 API")
@RestController
@RequiredArgsConstructor
public class StudyApplicationController {

    private final StudyApplicationCommandService studyApplicationCommandService;
    private final UserQueryService userQueryService;
    private final StudyApplicationQueryService studyApplicationQueryService;

    @Operation(
            summary = "스터디글 지원",
            description = "해당 스터디글에 로그인한 사용자가 지원합니다."
    )
    @PostMapping("/api/study-posts/{studyPostId}/applications")
    public ApiResponse<StudyApplicationResult> applyStudyPost(@PathVariable("studyPostId") Long studyPostId,  @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        StudyApplication studyApplication = studyApplicationCommandService.applyStudyApplication(studyPostId, user);
        StudyApplicationResult responseDTO = StudyApplicationConverter.toStudyApplicationResult(studyApplication);
        return ApiResponse.of(SuccessStatus._STUDY_APPLICATION_CREATED,responseDTO);
    }

    @Operation(summary = "스터디 지원자 전체 조회", description = "스터디글에 지원한 모든 지원자 목록을 조회합니다.")
    @GetMapping("/api/study-posts/{studyPostId}/applications")
    public ApiResponse<List<StudyApplicationListResponse>> getAllStudyApplication(@PathVariable("studyPostId") Long studyPostId) {
        List<StudyApplicationListResponse> list = studyApplicationQueryService.findAllApplicants(studyPostId);
        return ApiResponse.of(SuccessStatus._STUDY_APPLICANT_LIST_RETRIEVED, list);
    }

    @Operation(summary = "내가 지원한 스터디 목록 조회", description = "로그인한 사용자가 지원한 스터디 목록을 조회합니다.")
    @GetMapping("/api/users/applications")
    public ApiResponse<List<MyStudyApplicationResponse>> getMyStudyApplication(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        List<MyStudyApplicationResponse> list = studyApplicationQueryService.findMyStudyApplications(user.getId());
        return ApiResponse.of(SuccessStatus._STUDY_APPLICATION_MY_LIST_RETRIEVED, list);
    }

    @Operation(summary = "스터디 지원 승인", description = "스터디글 작성자가 해당 신청을 승인합니다.")
    @PatchMapping("/api/study-applications/{studyApplicationId}/approve")
    public ApiResponse<StudyApplicationApproveResponse> approveStudyApplication(@PathVariable("studyApplicationId") Long studyApplicationId, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        StudyApplication studyApplication = studyApplicationCommandService.approveStudyApplication(studyApplicationId,
                user);
        StudyApplicationApproveResponse responseDTO = StudyApplicationConverter.toApproveResponse(studyApplication);
        return ApiResponse.of(SuccessStatus._STUDY_APPLICATION_APPROVED, responseDTO);

    }

    @Operation(summary = "스터디 지원 거절", description = "스터디글 작성자가 해당 신청을 거절합니다.")
    @PatchMapping("/api/study-applications/{studyApplicationId}/reject")
    public ApiResponse<StudyApplicationRejectResponse> rejectStudyApplication(@PathVariable("studyApplicationId") Long studyApplicationId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        StudyApplication studyApplication = studyApplicationCommandService.rejectStudyApplication(studyApplicationId,
                user);
        StudyApplicationRejectResponse responseDTO = StudyApplicationConverter.toRejectResponse(studyApplication);
        return ApiResponse.of(SuccessStatus._STUDY_APPLICATION_REJECT, responseDTO);
    }
}
