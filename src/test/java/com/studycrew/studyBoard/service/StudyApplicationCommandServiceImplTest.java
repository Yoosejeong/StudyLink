package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import com.studycrew.studyBoard.repository.UserRepository;
import com.studycrew.studyBoard.service.studyApplication.StudyApplicationCommandService;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class StudyApplicationCommandServiceImplTest {

    @Autowired
    private StudyApplicationCommandService studyApplicationCommandService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudyPostRepository studyPostRepository;

    @Test
    void 스터디_지원하기_테스트(){
        User user = getUser();
        userRepository.save(user);
        StudyPost studyPost = getStudyPost(user);
        studyPostRepository.save(studyPost);
        StudyApplication studyApplication = studyApplicationCommandService.applyStudyApplication(studyPost.getId(),
                user);
        assertThat(studyApplication.getStudyPost()).isEqualTo(studyPost);
        assertThat(studyApplication.getUser()).isEqualTo(user);
        assertThat(studyApplication.getApplicationStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void 스터디_모집종료_상태_지원_예외발생() {
        User user =  getUser();
        userRepository.save(user);
        StudyPost closedStudyPost = getClosedStudyPost(user);
        studyPostRepository.save(closedStudyPost);
        Throwable thrown = catchThrowable(() ->
                studyApplicationCommandService.applyStudyApplication(closedStudyPost.getId(), user)
        );
        assertThat(thrown).isInstanceOf(StudyPostHandler.class);

        StudyPostHandler exception = (StudyPostHandler) thrown;
        assertThat(exception.getErrorReason().getCode()).isEqualTo("POST409");
        assertThat(exception.getErrorReason().getMessage()).contains("이미 모집이 종료된 스터디글입니다.");
    }

    @Test
    void 스터디_중복지원_예외발생() {
        User user = getUser();
        userRepository.save(user);
        StudyPost studyPost = getStudyPost(user);
        studyPostRepository.save(studyPost);
        studyApplicationCommandService.applyStudyApplication(studyPost.getId(), user);
        Throwable thrown = catchThrowable(() ->
                studyApplicationCommandService.applyStudyApplication(studyPost.getId(), user)
        );
        StudyPostHandler exception = (StudyPostHandler) thrown;
        assertThat(exception.getErrorReason().getCode()).isEqualTo("APPLICATION400");
        assertThat(exception.getErrorReason().getMessage()).contains("이미 해당 스터디에 지원했습니다.");

    }

    private static User getUser() {
        return User.builder()
                .email("이메일@email.com")
                .nickname("닉네임")
                .username("이름")
                .password("비밀번호")
                .role("ROLE_USER")
                .build();
    }

    private StudyPost getStudyPost(User user) {

        return StudyPost.builder()
                .title("제목")
                .content("내용")
                .studyStatus(StudyStatus.RECRUITING)
                .currentPeople(3)
                .maxPeople(23)
                .user(user)
                .build();
    }

    private StudyPost getClosedStudyPost(User user) {

        return StudyPost.builder()
                .title("제목")
                .content("내용")
                .studyStatus(StudyStatus.CLOSED)
                .currentPeople(3)
                .maxPeople(23)
                .user(user)
                .build();
    }

}