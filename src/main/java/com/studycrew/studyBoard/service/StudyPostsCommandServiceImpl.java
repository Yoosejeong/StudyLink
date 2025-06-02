package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.converter.StudyPostsConverter;
import com.studycrew.studyBoard.dto.StudyPostsDTO.StudyPostsRequestDTO.StudyPostsCreate;
import com.studycrew.studyBoard.entity.StudyPosts;
import com.studycrew.studyBoard.repository.StudyPostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyPostsCommandServiceImpl {

    private final StudyPostsRepository studyPostsRepository;

    public StudyPosts createStudyPosts(StudyPostsCreate dto){
        StudyPosts studyPosts = StudyPostsConverter.toStudyPosts(dto);
        return studyPostsRepository.save(studyPosts);
    }
}
