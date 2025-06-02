package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.converter.StudyPostConverter;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostCreate;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyPostCommandServiceImpl {

    private final StudyPostRepository studyPostRepository;

    public StudyPost createStudyPost(StudyPostCreate dto){
        StudyPost studyPost = StudyPostConverter.toStudyPost(dto);
        return studyPostRepository.save(studyPost);
    }
}
