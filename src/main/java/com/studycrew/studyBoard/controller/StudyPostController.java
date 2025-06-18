package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.StudyPostConverter;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostRequestUpdate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPost;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.service.studyPost.StudyPostCommandService;
import com.studycrew.studyBoard.service.studyPost.StudyPostQueryService;
import com.studycrew.studyBoard.service.user.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyPostController {

    private final StudyPostCommandService studyPostCommandService;
    private final UserQueryService userQueryService;
    private final StudyPostQueryService studyPostQueryService;

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

    @DeleteMapping("/api/study-posts/{studyPostId}")
    public ApiResponse<Void> deleteStudyPost(@PathVariable("studyPostId") Long studyPostId, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        studyPostCommandService.deleteStudyPost(studyPostId, user);
        return ApiResponse.of(SuccessStatus._STUDY_POST_DELETED);
    }

    @PatchMapping("/api/study-posts/{studyPostId}")
    public ApiResponse<StudyPostResponseDTO.GetStudyPost> updateStudyPost(@PathVariable("studyPostId") Long studyPostId, @RequestBody StudyPostRequestUpdate requestDTO, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        StudyPost studyPost = studyPostCommandService.updateStudyPost(studyPostId, user, requestDTO);
        StudyPostResponseDTO.GetStudyPost responseDTO = StudyPostConverter.toGetStudyPost(studyPost);
        return ApiResponse.of(SuccessStatus._STUDY_POST_UPDATE, responseDTO);
    }

    @GetMapping("/api/study-posts/{studyPostId}")
    public ApiResponse<StudyPostResponseDTO.GetStudyPost> getStudyPost(@PathVariable("studyPostId") Long studyPostId){
        StudyPost studyPost = studyPostQueryService.getStudyPost(studyPostId);
        GetStudyPost responseDTO = StudyPostConverter.toGetStudyPost(studyPost);
        return ApiResponse.of(SuccessStatus._STUDY_POST_RETRIEVED, responseDTO);
    }

    @GetMapping("/api/study-posts")
    public ApiResponse<Page<StudyPostResponseDTO.GetStudyPostListResponse>> getStudyPostList(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                                                            Pageable pageable) {
        Page<GetStudyPostListResponse> studyPostList = studyPostQueryService.getStudyPostList(pageable);
        return ApiResponse.of(SuccessStatus._STUDY_POST_LIST_RETRIEVED, studyPostList);
    }

    @PatchMapping("/api/study-posts/{studyPostId}/close")
    public ApiResponse<StudyPostResponseDTO.GetStudyPost> closeStudyPost(@PathVariable("studyPostId") Long studyPostId, @AuthenticationPrincipal CustomUserDetails customUserDetails){
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        StudyPost studyPost = studyPostCommandService.closeStudyPost(studyPostId, user);
        GetStudyPost responseDTO = StudyPostConverter.toGetStudyPost(studyPost);
        return ApiResponse.of(SuccessStatus._STUDY_POST_CLOSED, responseDTO);
    }
}
