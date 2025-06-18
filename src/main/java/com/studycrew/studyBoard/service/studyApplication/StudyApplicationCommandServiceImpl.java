package com.studycrew.studyBoard.service.studyApplication;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyApplicationHandler;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.converter.StudyApplicationConverter;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.repository.StudyApplicationRepository;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyApplicationCommandServiceImpl implements StudyApplicationCommandService {

    private final StudyPostRepository studyPostRepository;
    private final StudyApplicationRepository studyApplicationRepository;

    public StudyApplication applyStudyApplication(Long studyPostId, User user){
        StudyPost studyPost = studyPostRepository.findById(studyPostId)
                .orElseThrow(() -> new StudyPostHandler(ErrorStatus._STUDY_POST_NOT_FOUND));
        if (studyPost.getStudyStatus() == StudyStatus.CLOSED){
            throw new StudyPostHandler(ErrorStatus._STUDY_POST_ALREADY_CLOSED);
        }
        if (studyApplicationRepository.existsByStudyPostAndUser(studyPost, user)) {
            throw new StudyPostHandler(ErrorStatus._STUDY_APPLICATION_ALREADY_EXISTS);
        }
        StudyApplication studyApplication = StudyApplicationConverter.toPendingApplication(studyPost, user);
        return studyApplicationRepository.save(studyApplication);
    }

    @Override
    public StudyApplication approveStudyApplication(Long studyApplicationId, User user) {
        StudyApplication studyApplication = studyApplicationRepository.findById(studyApplicationId).orElseThrow( ()-> new StudyApplicationHandler(ErrorStatus._STUDY_APPLICATION_NOT_FOUND));

        if(!studyApplication.getStudyPost().getUser().getId().equals(user.getId())) {
            throw new StudyApplicationHandler(ErrorStatus._STUDY_APPLICATION_FORBIDDEN);
        }

        studyApplication.approve();
        studyApplication.getStudyPost().increaseAcceptedPeople();
        return studyApplication;
    }

    @Override
    public StudyApplication rejectStudyApplication(Long studyApplicationId, User user) {
        StudyApplication studyApplication = studyApplicationRepository.findById(studyApplicationId).orElseThrow( ()-> new StudyApplicationHandler(ErrorStatus._STUDY_APPLICATION_NOT_FOUND));

        if(!studyApplication.getStudyPost().getUser().getId().equals(user.getId())) {
            throw new StudyApplicationHandler(ErrorStatus._STUDY_APPLICATION_FORBIDDEN);
        }

        studyApplication.reject();
        return studyApplication;
    }
}
