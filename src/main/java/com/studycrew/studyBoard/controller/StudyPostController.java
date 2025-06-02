package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.StudyPostConverter;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPost;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.service.StudyPostCommandService;
import com.studycrew.studyBoard.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyPostController {

    private final StudyPostCommandService studyPostCommandService;
    private final UserQueryService userQueryService;

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

}
