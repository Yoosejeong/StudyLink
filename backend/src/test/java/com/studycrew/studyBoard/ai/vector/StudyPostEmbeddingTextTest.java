package com.studycrew.studyBoard.ai.vector;

import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.Category;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class StudyPostEmbeddingTextTest {

    @Test
    void 제목_카테고리_본문으로_임베딩_텍스트를_만든다() {
        StudyPost post = StudyPost.builder()
                .title("AWS 인프라 스터디")
                .category(Category.CLOUD_DEVOPS)
                .content("EC2, RDS, VPC를 중심으로 인프라를 구성합니다.")
                .build();

        assertThat(StudyPostEmbeddingText.of(post))
                .isEqualTo("제목: AWS 인프라 스터디\n카테고리: 클라우드/DevOps\n내용: EC2, RDS, VPC를 중심으로 인프라를 구성합니다.");
    }

    @Test
    void 비어있는_값은_빈_문자열로_처리한다() {
        assertThat(StudyPostEmbeddingText.of(StudyPost.builder().build()))
                .isEqualTo("제목: \n카테고리: \n내용: ");
    }

    @Test
    void 모든_카테고리에_한국어_라벨이_있다() {
        assertThat(Arrays.stream(Category.values()).map(StudyPostEmbeddingText::categoryLabel))
                .doesNotContainAnyElementsOf(Arrays.stream(Category.values()).map(Category::name).toList());
    }
}
