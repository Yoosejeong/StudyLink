package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyPostQueryServiceImpl implements StudyPostQueryService {

    private final StudyPostRepository studyPostRepository;

    @Override
    public StudyPost getStudyPost(Long studyPostId) {
        return studyPostRepository.findById(studyPostId).orElseThrow(() -> new StudyPostHandler(
                ErrorStatus._STUDY_POST_NOT_FOUND));
    }
}
