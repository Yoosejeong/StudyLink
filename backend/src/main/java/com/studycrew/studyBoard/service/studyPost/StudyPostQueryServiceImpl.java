package com.studycrew.studyBoard.service.studyPost;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.config.props.S3Props;
import com.studycrew.studyBoard.converter.StudyPostConverter;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import com.studycrew.studyBoard.util.S3UrlUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyPostQueryServiceImpl implements StudyPostQueryService {

    private final StudyPostRepository studyPostRepository;
    private final S3UrlUtil s3UrlUtil;

    @Override
    public GetStudyPostAndProfile getStudyPost(Long studyPostId) {
        StudyPost studyPost = studyPostRepository.findWithUserAndTagsByIdAndDeletedFalse(studyPostId).orElseThrow(() -> new StudyPostHandler(
                ErrorStatus._STUDY_POST_NOT_FOUND));
        String key = studyPost.getUser().getProfileKey();
        String url = s3UrlUtil.buildPublicUrl(key);
        return StudyPostConverter.toGetStudyPostAndProfile(studyPost, url);
    }

    @Override
    public Page<GetStudyPostListResponse> getStudyPostList(String rawKeyword, StudyStatus status, Pageable pageable) {
        Page<GetStudyPostListResponse> page =
                studyPostRepository.searchByStatusAndNotDeleted(rawKeyword, status, pageable);

        return page.map(dto -> {
            dto.setProfileUrl(s3UrlUtil.buildPublicUrl(dto.getProfileKey()));
            return dto;
        });
    }

}
