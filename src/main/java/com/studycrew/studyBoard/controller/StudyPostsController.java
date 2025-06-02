package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.StudyPostsConverter;
import com.studycrew.studyBoard.dto.StudyPostsDTO.StudyPostsRequestDTO;
import com.studycrew.studyBoard.dto.StudyPostsDTO.StudyPostsRequestDTO.StudyPostsCreate;
import com.studycrew.studyBoard.dto.StudyPostsDTO.StudyPostsResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostsDTO.StudyPostsResponseDTO.GetStudyPosts;
import com.studycrew.studyBoard.entity.StudyPosts;
import com.studycrew.studyBoard.service.StudyPostsCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudyPostsController {

    StudyPostsCommandService studyPostsCommandService;

    @PostMapping("/api/study-posts")
    public ResponseEntity<ApiResponse<StudyPostsResponseDTO.GetStudyPosts>> createStudyPosts(@RequestBody StudyPostsCreate requestDTO){
        StudyPosts studyPosts = studyPostsCommandService.createStudyPosts(requestDTO);
        GetStudyPosts responseDTO = StudyPostsConverter.toGetStudyPosts(studyPosts);
        return ResponseEntity
                .status(SuccessStatus._STUDY_POST_CREATED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus._STUDY_POST_CREATED, responseDTO));
    }

}
