package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostRequestUpdate;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import com.studycrew.studyBoard.repository.UserRepository;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class StudyPostCommandServiceImplTest {

    @Autowired
    StudyPostCommandService studyPostCommandService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StudyPostRepository studyPostRepository;
    @Autowired
    StudyPostQueryService studyPostQueryService;

    @Test
    void createStudyPost(){
        User user = getUser();

        StudyPost studyPost = getStudyPost(user);

        assertThat(studyPost.getContent()).isEqualTo("내용");
    }

    private StudyPost getStudyPost(User user) {
        StudyPostRequestDTO.StudyPostCreate requestDTO = StudyPostRequestDTO.StudyPostCreate.builder()
                .title("제목")
                .content("내용")
                .current_people(5)
                .max_people(10)
                .build();

        StudyPost studyPost = studyPostCommandService.createStudyPost(requestDTO, user);
        return studyPost;
    }


    @Test
    void deleteStudyPost(){

    }

    @Test
    void updateStudyPost(){

        StudyPostRequestDTO.StudyPostRequestUpdate requestDTO = StudyPostRequestUpdate
                .builder()
                .title("바뀐제목")
                .content("바뀐내용")
                .build();

        User user= getUser();
        userRepository.save(user);
        StudyPost studyPost = getStudyPost(user);
        studyPostRepository.save(studyPost);
        StudyPost updateStudyPost = studyPostCommandService.updateStudyPost(studyPost.getId(), user, requestDTO);

        assertThat(updateStudyPost.getTitle()).isEqualTo("바뀐제목");
    }

    @Test
    void 스터디글_단건_조회() {
        User user = userRepository.save(getUser());
        StudyPost post = studyPostRepository.save(getStudyPost(user));

        StudyPost findPost = studyPostQueryService.getStudyPost(post.getId());

        assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
        assertThat(findPost.getContent()).isEqualTo(post.getContent());
        assertThat(findPost.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void 스터디글_단건_조회_예외_없는_ID() {
        Long invalidId = 999L;

        Throwable thrown = catchThrowable(() -> studyPostQueryService.getStudyPost(invalidId));

        assertThat(thrown).isInstanceOf(StudyPostHandler.class);

        StudyPostHandler exception = (StudyPostHandler) thrown;

        assertThat(exception.getCode()).isEqualTo(ErrorStatus._STUDY_POST_NOT_FOUND);
        assertThat(exception.getErrorReason().getCode()).isEqualTo("POST404");
        assertThat(exception.getErrorReason().getIsSuccess()).isFalse();
    }

    private static User getUser() {
        User user = User.builder()
                .email("이메일@email.com")
                .nickname("닉네임")
                .username("이름")
                .password("비밀번호")
                .role("ROLE_USER")
                .build();
        return user;
    }

}