package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.exception.handler.StudyApplicationHandler;
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
        assertThat(exception.getErrorReason().getCode()).isEqualTo("POST4090");
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
        assertThat(exception.getErrorReason().getCode()).isEqualTo("APPLICATION4090");
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

    @Test
    void 스터디_승인_완료() {
        // given: 글 작성자와 지원자 생성 및 저장
        User user = getUser();
        User user2 = getUser2();
        userRepository.save(user);
        userRepository.save(user2);

        StudyPost studyPost = getStudyPost(user);
        studyPostRepository.save(studyPost);

        // when: 지원자가 스터디에 지원하고, 승인 처리
        StudyApplication studyApplication = studyApplicationCommandService.applyStudyApplication(studyPost.getId(),
                user2);
        StudyApplication approvedStudyApplication = studyApplicationCommandService.approveStudyApplication(
                studyApplication.getId(), user);

        // then: 승인 상태로 바뀌었는지 확인
        assertThat(approvedStudyApplication.getApplicationStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        // then: 해당 스터디 글과 연결되어 있는지 확인
        assertThat(approvedStudyApplication.getStudyPost().getId()).isEqualTo(studyPost.getId());
        // then: 승인된 인원이 1 증가했는지 확인
        assertThat(approvedStudyApplication.getStudyPost().getAcceptedPeople()).isEqualTo(1);

    }

    @Test
    void 이미_승인된_신청에_승인_요청시에_예외발생() {
        // given: 글 작성자와 지원자 생성 및 저장
        User user = getUser();
        User user2 = getUser2();
        userRepository.save(user);
        userRepository.save(user2);

        StudyPost studyPost = getStudyPost(user);
        studyPostRepository.save(studyPost);

        // when: 지원자가 스터디에 지원하고, 승인 처리
        StudyApplication studyApplication = studyApplicationCommandService.applyStudyApplication(studyPost.getId(),
                user2);
        StudyApplication approvedStudyApplication = studyApplicationCommandService.approveStudyApplication(
                studyApplication.getId(), user);

        // when & then: 2차 승인 → 예외 발생해야 함
        Throwable thrown = catchThrowable(() ->
                studyApplicationCommandService.approveStudyApplication(studyApplication.getId(), user)
        );

        StudyApplicationHandler exception = (StudyApplicationHandler) thrown;

        assertThat(exception.getErrorReason().getCode()).isEqualTo("APPLICATION4091");
        assertThat(exception.getErrorReason().getMessage()).contains("이미 처리된 지원입니다.");
    }

    @Test
    void 스터디_지원_거절_완료() {
        // given: 글 작성자와 지원자 생성 및 저장
        User user = getUser();
        User user2 = getUser2();
        userRepository.save(user);
        userRepository.save(user2);

        StudyPost studyPost = getStudyPost(user);
        studyPostRepository.save(studyPost);

        // when: 지원자가 스터디에 지원하고, 거절 처리
        StudyApplication studyApplication = studyApplicationCommandService.applyStudyApplication(studyPost.getId(),
                user2);
        StudyApplication rejectStudyApplication = studyApplicationCommandService.rejectStudyApplication(
                studyApplication.getId(), user);

        // then: 지원한 스터디 status가 REJECT인지 확인
        assertThat(rejectStudyApplication.getApplicationStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void 자신의_스터디글_아닐경우_지원_거절_불가_예외() {
        // given: 글 작성자와 지원자 생성 및 저장
        User user = getUser();
        User user2 = getUser2();
        userRepository.save(user);
        userRepository.save(user2);

        StudyPost studyPost = getStudyPost(user);
        studyPostRepository.save(studyPost);

        // when: 지원자가 스터디에 지원
        StudyApplication studyApplication = studyApplicationCommandService.applyStudyApplication(studyPost.getId(),
                user2);

        // when & then: 글 작성자가 아닌 회원이 거절 할 경우 예외 발생
        Throwable thrown = catchThrowable(() ->  studyApplicationCommandService.rejectStudyApplication(
                studyApplication.getId(), user2) );

        StudyApplicationHandler exception = (StudyApplicationHandler) thrown;

        assertThat(exception.getErrorReason().getCode()).isEqualTo("APPLICATION4030");
        assertThat(exception.getErrorReason().getMessage()).contains("스터디 지원에 권한이 없습니다.");

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