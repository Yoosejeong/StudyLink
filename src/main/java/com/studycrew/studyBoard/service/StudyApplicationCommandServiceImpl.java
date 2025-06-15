package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyApplicationCommandServiceImpl implements StudyApplicationCommandService{

    private final StudyPostRepository studyPostRepository;

    StudyApplication applyStudyApplication(Long studyPostId, User user){
        StudyPost studyPost = studyPostRepository.findById(studyPostId)
                .orElseThrow(() -> new StudyPostHandler(ErrorStatus._STUDY_POST_NOT_FOUND));
    }
}
