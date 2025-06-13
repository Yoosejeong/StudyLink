package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.converter.StudyPostConverter;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostRequestUpdate;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudyPostCommandServiceImpl implements StudyPostCommandService{

    private final StudyPostRepository studyPostRepository;

    public StudyPost createStudyPost(StudyPostCreate dto, User user){
        StudyPost studyPost = StudyPostConverter.toStudyPost(dto, user);
        return studyPostRepository.save(studyPost);
    }

    @Override
    public void deleteStudyPost(Long studyPostId, User user) {
        StudyPost studyPost = studyPostRepository.findById(studyPostId)
                .orElseThrow(()-> new StudyPostHandler(ErrorStatus._STUDY_POST_NOT_FOUND));
        if (!studyPost.getUser().getId().equals(user.getId())){
            throw new StudyPostHandler(ErrorStatus._STUDY_POST_FORBIDDEN);
        }
        studyPostRepository.deleteById(studyPostId);
    }

    @Override
    public StudyPost updateStudyPost(Long studyPostId, User user, StudyPostRequestUpdate dto) {
        StudyPost studyPost = studyPostRepository.findById(studyPostId)
                .orElseThrow(() -> new StudyPostHandler(ErrorStatus._STUDY_POST_NOT_FOUND));
        if (!studyPost.getUser().getId().equals(user.getId())){
            throw new StudyPostHandler(ErrorStatus._STUDY_POST_FORBIDDEN);
        }
        studyPost.update(dto.getTitle(), dto.getContent());
        return studyPost;
    }

    @Override
    public StudyPost closeStudyPost(Long studyPostId, User user) {
        StudyPost studyPost = studyPostRepository.findById(studyPostId)
                .orElseThrow(() -> new StudyPostHandler(ErrorStatus._STUDY_POST_NOT_FOUND));
        if (!studyPost.getUser().getId().equals(user.getId())){
            throw new StudyPostHandler(ErrorStatus._STUDY_POST_FORBIDDEN);
        }
        studyPost.closeStudyPost();
        return studyPost;
    }
}
