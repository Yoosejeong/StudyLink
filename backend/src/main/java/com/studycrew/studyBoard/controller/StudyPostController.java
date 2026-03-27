package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.StudyPostConverter;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostRequestUpdate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPost;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.StudyPostCursorResponse;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.service.studyPost.StudyPostCommandService;
import com.studycrew.studyBoard.service.studyPost.StudyPostQueryService;
import com.studycrew.studyBoard.service.user.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "스터디 모집글", description = "스터디 모집글 관련 API")
@RestController
@RequiredArgsConstructor
@Validated
public class StudyPostController {

    private final StudyPostCommandService studyPostCommandService;
    private final UserQueryService userQueryService;
    private final StudyPostQueryService studyPostQueryService;

    @Operation(summary = "스터디 모집글 생성", description = "스터디 모집글을 생성합니다.")
    @PostMapping("/api/study-posts")
    public ResponseEntity<ApiResponse<StudyPostResponseDTO.GetStudyPost>> createStudyPost(@RequestBody StudyPostCreate requestDTO, @AuthenticationPrincipal CustomUserDetails userDetails){
        String email = userDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);

        StudyPost studyPost = studyPostCommandService.createStudyPost(requestDTO, user);
        GetStudyPost responseDTO = StudyPostConverter.toGetStudyPost(studyPost);
        return ResponseEntity
                .status(SuccessStatus._STUDY_POST_CREATED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus._STUDY_POST_CREATED, responseDTO));
    }

    @Operation(summary = "스터디 모집글 삭제", description = "스터디 모집글을 삭제합니다.")
    @DeleteMapping("/api/study-posts/{studyPostId}")
    public ApiResponse<Void> deleteStudyPost(@PathVariable("studyPostId") Long studyPostId, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        studyPostCommandService.deleteStudyPost(studyPostId, user);
        return ApiResponse.of(SuccessStatus._STUDY_POST_DELETED);
    }

    @Operation(summary = "스터디 모집글 수정", description = "스터디 모집글의 제목과 내용을 수정합니다.")
    @PatchMapping("/api/study-posts/{studyPostId}")
    public ApiResponse<StudyPostResponseDTO.GetStudyPost> updateStudyPost(@PathVariable("studyPostId") Long studyPostId, @RequestBody StudyPostRequestUpdate requestDTO, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        StudyPost studyPost = studyPostCommandService.updateStudyPost(studyPostId, user, requestDTO);
        StudyPostResponseDTO.GetStudyPost responseDTO = StudyPostConverter.toGetStudyPost(studyPost);
        return ApiResponse.of(SuccessStatus._STUDY_POST_UPDATE, responseDTO);
    }

    @Operation(summary = "스터디 모집글 단건 조회", description = "스터디 모집글 ID로 글을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "스터디글이 존재하지 않습니다.")
    })
    @GetMapping("/api/study-posts/{studyPostId}")
    public ApiResponse<StudyPostResponseDTO.GetStudyPostAndProfile> getStudyPost(@PathVariable("studyPostId") Long studyPostId){
        StudyPostResponseDTO.GetStudyPostAndProfile responseDTO = studyPostQueryService.getStudyPost(studyPostId);
        return ApiResponse.of(SuccessStatus._STUDY_POST_RETRIEVED, responseDTO);
    }

    @Operation(summary = "스터디 모집글 목록 조회", description = "커서 기반 페이징으로 스터디 모집글을 조회합니다.")
    @GetMapping("/api/study-posts")
    public ApiResponse<StudyPostCursorResponse> getStudyPostList(
            @RequestParam(required = false) @Size(min = 2, max = 50, message = "검색어는 2~50자 사이여야합니다.") String rawKeyword,
            @RequestParam(required = false) StudyStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastCreatedAt,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size) {
        StudyPostCursorResponse result = studyPostQueryService.getStudyPostList(rawKeyword, status, lastCreatedAt, lastId, size);
        return ApiResponse.of(SuccessStatus._STUDY_POST_LIST_RETRIEVED, result);
    }

    @Operation(summary = "스터디 모집 마감", description = "스터디 모집글을 마감 상태로 변경합니다.")
    @PatchMapping("/api/study-posts/{studyPostId}/close")
    public ApiResponse<StudyPostResponseDTO.GetStudyPost> closeStudyPost(@PathVariable("studyPostId") Long studyPostId, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        StudyPost studyPost = studyPostCommandService.closeStudyPost(studyPostId, user);
        GetStudyPost responseDTO = StudyPostConverter.toGetStudyPost(studyPost);
        return ApiResponse.of(SuccessStatus._STUDY_POST_CLOSED, responseDTO);
    }

}
