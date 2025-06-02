package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostRequestDTO;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import org.assertj.core.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class StudyPostCommandServiceImplTest {

    @Autowired
    StudyPostCommandService service;

    @Test
    void createStudyPost(){


        StudyPostRequestDTO.StudyPostCreate requestDTO = StudyPostRequestDTO.StudyPostCreate.builder()
                .title("제목")
                .content("내용")
                .current_people(5)
                .max_people(10)
                .build();

        User user = User.builder()
                .email("이메일@email.com")
                .nickname("닉네임")
                .username("이름")
                .password("비밀번호")
                .role("ROLE_USER")
                .build();

        StudyPost studyPost = service.createStudyPost(requestDTO, user);

        Assertions.assertThat(studyPost.getContent()).isEqualTo("내용");
    }

}