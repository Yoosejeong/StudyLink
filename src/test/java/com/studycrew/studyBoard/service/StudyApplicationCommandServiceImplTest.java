package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO;
import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import com.studycrew.studyBoard.repository.UserRepository;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
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

}