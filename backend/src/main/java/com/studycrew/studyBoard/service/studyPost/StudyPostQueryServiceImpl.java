package com.studycrew.studyBoard.service.studyPost;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.converter.StudyPostConverter;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyPostQueryServiceImpl implements StudyPostQueryService {

    private final StudyPostRepository studyPostRepository;

    @Override
    public StudyPost getStudyPost(Long studyPostId) {
        return studyPostRepository.findByIdAndDeletedFalse(studyPostId).orElseThrow(() -> new StudyPostHandler(
                ErrorStatus._STUDY_POST_NOT_FOUND));
    }

    @Override
    public Page<StudyPostResponseDTO.GetStudyPostListResponse> getStudyPostList(Pageable pageable) {
        return studyPostRepository.findAllByDeletedFalse(pageable)
                .map(StudyPostConverter::toGetStudyPostList);
    }
}
