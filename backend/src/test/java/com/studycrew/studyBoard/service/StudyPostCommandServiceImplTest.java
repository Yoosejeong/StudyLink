package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO.StudyPostRequestUpdate;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.StudyPostCursorResponse;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import com.studycrew.studyBoard.repository.UserRepository;
import com.studycrew.studyBoard.service.studyApplication.StudyApplicationCommandService;
import com.studycrew.studyBoard.service.studyPost.StudyPostCommandService;
import com.studycrew.studyBoard.service.studyPost.StudyPostQueryService;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    @Autowired
    StudyApplicationCommandService studyApplicationCommandService;

    @Test
    void 스터디글_생성(){
        User user = getUser();

        StudyPost studyPost = getStudyPost(user, 1);

        assertThat(studyPost.getContent()).isEqualTo("내용");
    }

    private StudyPost getStudyPost(User user, int index) {
        StudyPostRequestDTO.StudyPostCreate requestDTO = StudyPostRequestDTO.StudyPostCreate.builder()
                .title("제목")
                .content("내용")
                .maxPeople(10)
                .build();

        StudyPost studyPost = studyPostCommandService.createStudyPost(requestDTO, user);
        return studyPost;
    }


    @Test
    void 스터디글_삭제(){
        User user = getUser();
        userRepository.save(user);

        StudyPost studyPost = getStudyPost(user, 1);
        studyPostRepository.save(studyPost);

        studyPostCommandService.deleteStudyPost(studyPost.getId(), user);

        assertThat(studyPost.isDeleted()).isEqualTo(true);
        assertThat(studyPost.getDeletedAt()).isNotNull();
    }

    @Test
    void 스터디글_수정(){

        StudyPostRequestDTO.StudyPostRequestUpdate requestDTO = StudyPostRequestUpdate
                .builder()
                .title("바뀐제목")
                .content("바뀐내용")
                .build();

        User user= getUser();
        userRepository.save(user);
        StudyPost studyPost = getStudyPost(user, 1);
        studyPostRepository.save(studyPost);
        StudyPost updateStudyPost = studyPostCommandService.updateStudyPost(studyPost.getId(), user, requestDTO);

        assertThat(updateStudyPost.getTitle()).isEqualTo("바뀐제목");
    }

    @Test
    void 스터디글_단건_조회() {
        User user = userRepository.save(getUser());
        StudyPost post = studyPostRepository.save(getStudyPost(user, 1));

        StudyPostResponseDTO.GetStudyPostAndProfile findPost = studyPostQueryService.getStudyPost(post.getId());

        assertThat(findPost.getTitle()).isEqualTo(post.getTitle());
        assertThat(findPost.getContent()).isEqualTo(post.getContent());
        assertThat(findPost.getUserId()).isEqualTo(user.getId());
    }

    @Test
    void 스터디글_단건_조회_예외_없는_ID() {
        Long invalidId = 999L;

        Throwable thrown = catchThrowable(() -> studyPostQueryService.getStudyPost(invalidId));

        assertThat(thrown).isInstanceOf(StudyPostHandler.class);

        StudyPostHandler exception = (StudyPostHandler) thrown;

        assertThat(exception.getCode()).isEqualTo(ErrorStatus._STUDY_POST_NOT_FOUND);
        assertThat(exception.getErrorReason().getCode()).isEqualTo("POST4040");
        assertThat(exception.getErrorReason().getIsSuccess()).isFalse();
    }

    @Test
    void 스터디글_목록_조회() {
        User user = userRepository.save(getUser());

        for (int i = 0; i < 3; i++) {
            studyPostRepository.save(getStudyPost(user, i));
        }

        StudyPostCursorResponse result = studyPostQueryService.getStudyPostList(null, null, null, null, 10);

        assertThat(result.getItems()).hasSize(3);
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    void 스터디글_모집_종료() {
        User user = getUser();
        userRepository.save(user);
        StudyPost studyPost = getStudyPost(user, 1);
        studyPostRepository.save(studyPost);
        StudyPost closedStudyPost = studyPostCommandService.closeStudyPost(studyPost.getId(), user);
        assertThat(closedStudyPost.getStudyStatus()).isEqualTo(StudyStatus.CLOSED);
    }

    @Test
    void 삭제된_스터디글_모집_종료시_예외() {
        // given: 회원, 스터디글 생성
        User user = getUser();
        userRepository.save(user);
        StudyPost studyPost = getStudyPost(user, 1);
        studyPostRepository.save(studyPost);

        // when: 스터디글 삭제
        studyPostCommandService.deleteStudyPost(studyPost.getId(), user);

        // then: 삭제된 스터디글에 모집 종료시 예외 발생
        Throwable thrown = catchThrowable(() ->  studyPostCommandService.closeStudyPost(
                studyPost.getId(), user));
        StudyPostHandler exception = (StudyPostHandler) thrown;

        assertThat(exception.getErrorReason().getCode()).isEqualTo("POST4040");
        assertThat(exception.getErrorReason().getMessage()).contains("스터디글이 없습니다.");
    }

    @Test
    void 삭제된_스터디글_수정_예외() {
        // given: 회원, 스터디글, 수정 requestDTO 생성
        User user = getUser();
        userRepository.save(user);
        StudyPost studyPost = getStudyPost(user, 1);
        studyPostRepository.save(studyPost);

        StudyPostRequestUpdate dto = StudyPostRequestUpdate.builder()
                .title("수정제목")
                .content("수정글")
                .build();

        // when: 스터디글 삭제
        studyPostCommandService.deleteStudyPost(studyPost.getId(), user);

        // then: 삭제된 스터디글에 수정시 예외 발생
        Throwable thrown = catchThrowable(() ->  studyPostCommandService.updateStudyPost(
                studyPost.getId(), user, dto));
        StudyPostHandler exception = (StudyPostHandler) thrown;

        assertThat(exception.getErrorReason().getCode()).isEqualTo("POST4040");
        assertThat(exception.getErrorReason().getMessage()).contains("스터디글이 없습니다.");
    }

    @Test
    void 모집마감시_처리안된_지원_자동거절() {
        User user = getUser();
        User user2 = getUser2();

        userRepository.save(user);
        userRepository.save(user2);

        StudyPost studyPost = getStudyPost(user, 1);
        studyPostRepository.save(studyPost);

        StudyApplication studyApplication = studyApplicationCommandService.applyStudyApplication(studyPost.getId(),
                user2);
        studyPostCommandService.closeStudyPost(studyPost.getId(), user);

        assertThat(studyApplication.getApplicationStatus()).isEqualTo(ApplicationStatus.REJECTED);

    }

    @Test
    void 스터디글_모집중만_전체조회() {
        User user = userRepository.save(getUser());
        StudyPost studyPost = studyPostRepository.save(getStudyPost(user, 1));
        StudyPost studyPost2 = studyPostRepository.save(getStudyPost(user, 2));

        studyPostCommandService.closeStudyPost(studyPost2.getId(), user);

        StudyPostCursorResponse result = studyPostQueryService.getStudyPostList(null, StudyStatus.RECRUITING, null, null, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.isHasNext()).isFalse();
    }

    @Test
    void 스터디글_목록_조회_hasNext_true() {
        User user = userRepository.save(getUser());

        for (int i = 0; i < 5; i++) {
            studyPostRepository.save(getStudyPost(user, i));
        }

        StudyPostCursorResponse result = studyPostQueryService.getStudyPostList(null, null, null, null, 3);

        assertThat(result.getItems()).hasSize(3);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursorCreatedAt()).isNotNull();
        assertThat(result.getNextCursorId()).isNotNull();
    }

    @Test
    void 스터디글_목록_커서로_다음_페이지_조회() {
        User user = userRepository.save(getUser());

        for (int i = 0; i < 5; i++) {
            studyPostRepository.save(getStudyPost(user, i));
        }

        // 첫 번째 페이지 (size=3)
        StudyPostCursorResponse firstPage = studyPostQueryService.getStudyPostList(null, null, null, null, 3);

        assertThat(firstPage.getItems()).hasSize(3);
        assertThat(firstPage.isHasNext()).isTrue();

        // 두 번째 페이지 (첫 페이지 커서 사용)
        StudyPostCursorResponse secondPage = studyPostQueryService.getStudyPostList(
                null, null,
                firstPage.getNextCursorCreatedAt(),
                firstPage.getNextCursorId(),
                3
        );

        assertThat(secondPage.getItems()).hasSize(2);
        assertThat(secondPage.isHasNext()).isFalse();

        // 첫 페이지 + 두 번째 페이지 id 합치면 전체 5개, 중복 없음
        var firstIds = firstPage.getItems().stream().map(d -> d.getStudyPostId()).toList();
        var secondIds = secondPage.getItems().stream().map(d -> d.getStudyPostId()).toList();
        assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);
    }

    @Test
    void 스터디글_목록_키워드_검색() {
        User user = userRepository.save(getUser());

        studyPostRepository.save(createStudyPostWithTitle(user, "Spring Boot 스터디"));
        studyPostRepository.save(createStudyPostWithTitle(user, "React 스터디"));
        studyPostRepository.save(createStudyPostWithTitle(user, "알고리즘 모임"));

        StudyPostCursorResponse result = studyPostQueryService.getStudyPostList("스터디", null, null, null, 10);

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems())
                .extracting(d -> d.getTitle())
                .allMatch(title -> title.contains("스터디"));
    }

    @Test
    void 스터디글_목록_키워드_검색_결과_없음() {
        User user = userRepository.save(getUser());
        studyPostRepository.save(createStudyPostWithTitle(user, "Spring Boot 스터디"));

        StudyPostCursorResponse result = studyPostQueryService.getStudyPostList("없는키워드", null, null, null, 10);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursorId()).isNull();
    }

    @Test
    void 삭제된_스터디글_목록_미노출() {
        User user = userRepository.save(getUser());
        StudyPost post = studyPostRepository.save(getStudyPost(user, 1));
        studyPostRepository.save(getStudyPost(user, 2));

        studyPostCommandService.deleteStudyPost(post.getId(), user);

        StudyPostCursorResponse result = studyPostQueryService.getStudyPostList(null, null, null, null, 10);

        assertThat(result.getItems()).hasSize(1);
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

    private StudyPost createStudyPostWithTitle(User user, String title) {
        StudyPostRequestDTO.StudyPostCreate requestDTO = StudyPostRequestDTO.StudyPostCreate.builder()
                .title(title)
                .content("내용")
                .maxPeople(10)
                .build();
        return studyPostCommandService.createStudyPost(requestDTO, user);
    }

    private static User getUser2() {
        User user = User.builder()
                .email("이메일2@email.com")
                .nickname("닉네임2")
                .username("이름2")
                .password("비밀번호2")
                .role("ROLE_USER")
                .build();
        return user;
    }
}
