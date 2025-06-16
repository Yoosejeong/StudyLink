package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.MyStudyApplicationResponse;
import com.studycrew.studyBoard.dto.StudyApplicationDTO.StudyApplicationResponseDTO.StudyApplicationListResponse;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import com.studycrew.studyBoard.repository.UserRepository;
import com.studycrew.studyBoard.service.studyApplication.StudyApplicationCommandService;
import com.studycrew.studyBoard.service.studyApplication.StudyApplicationQueryService;
import java.util.List;
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
    @Autowired
    private StudyApplicationQueryService studyApplicationQueryService;

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
    void 스터디_지원한_회원목록_조회() {
        User user1 = getUser();
        User user2 = getUser2();
        User user3 = getUser3();
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        StudyPost studyPost = getStudyPost(user1);
        studyPostRepository.save(studyPost);

        studyApplicationCommandService.applyStudyApplication(studyPost.getId(), user2);
        studyApplicationCommandService.applyStudyApplication(studyPost.getId(), user3);
        List<StudyApplicationListResponse> list = studyApplicationQueryService.findAllApplicants(
                studyPost.getId());
        assertThat(list.size()).isEqualTo(2);
        assertThat(list).extracting("userId")
                .containsExactlyInAnyOrder(user2.getId(), user3.getId());
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

    @Test
    void 회원이_지원한_스터디_목록_조회() {
        User user = getUser();
        User user2 = getUser2();
        userRepository.save(user);
        userRepository.save(user2);

        StudyPost studyPost = getStudyPost(user);
        StudyPost studyPost2 = getStudyPost2(user);
        studyPostRepository.save(studyPost);
        studyPostRepository.save(studyPost2);

        studyApplicationCommandService.applyStudyApplication(studyPost.getId(), user2);
        studyApplicationCommandService.applyStudyApplication(studyPost2.getId(), user2);

        List<MyStudyApplicationResponse> list = studyApplicationQueryService.findMyStudyApplications(
                user2.getId());

        assertThat(list.size()).isEqualTo(2);
        assertThat(list).extracting(MyStudyApplicationResponse::getStudyPostId)
                .containsExactlyInAnyOrder(studyPost.getId(), studyPost2.getId());
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

    private static User getUser2() {
        return User.builder()
                .email("이메일2@email.com")
                .nickname("닉네임2")
                .username("이름2")
                .password("비밀번호2")
                .role("ROLE_USER")
                .build();
    }

    private static User getUser3() {
        return User.builder()
                .email("이메일3@email.com")
                .nickname("닉네임3")
                .username("이름3")
                .password("비밀번호3")
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

    private StudyPost getStudyPost2(User user) {

        return StudyPost.builder()
                .title("제목2")
                .content("내용2")
                .studyStatus(StudyStatus.RECRUITING)
                .currentPeople(2)
                .maxPeople(24)
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