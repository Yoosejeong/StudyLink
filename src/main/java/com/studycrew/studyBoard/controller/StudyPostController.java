package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.StudyPostConverter;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPost;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.service.StudyPostCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyPostController {

    StudyPostCommandService studyPostCommandService;

    @PostMapping("/api/study-posts")
    public ResponseEntity<ApiResponse<StudyPostResponseDTO.GetStudyPost>> createStudyPost(@RequestBody StudyPostCreate requestDTO){
        StudyPost studyPost = studyPostCommandService.createStudyPost(requestDTO);
        GetStudyPost responseDTO = StudyPostConverter.toGetStudyPost(studyPost);
        return ResponseEntity
                .status(SuccessStatus._STUDY_POST_CREATED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus._STUDY_POST_CREATED, responseDTO));
    }

}
