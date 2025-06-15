package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.converter.StudyApplicationConverter;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import com.studycrew.studyBoard.repository.StudyApplicationRepository;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyApplicationCommandServiceImpl implements StudyApplicationCommandService{

    private final StudyPostRepository studyPostRepository;
    private final StudyApplicationRepository studyApplicationRepository;

    public StudyApplication applyStudyApplication(Long studyPostId, User user){
        StudyPost studyPost = studyPostRepository.findById(studyPostId)
                .orElseThrow(() -> new StudyPostHandler(ErrorStatus._STUDY_POST_NOT_FOUND));
        StudyApplication studyApplication = StudyApplicationConverter.toPendingApplication(studyPost, user);
        return studyApplicationRepository.save(studyApplication);
    }
}
