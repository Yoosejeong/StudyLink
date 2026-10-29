package com.studycrew.studyBoard.ai.vector;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gemini Embedding 자동 설정은 API 키가 없으면 앱 시작을 실패시키므로
 * 기본 설정에서는 제외되고 app.vector.enabled=true 일 때만 Vector 검색 빈이 만들어지는지 확인한다.
 */
class VectorSearchProfileContextTest {

    @Nested
    @SpringBootTest
    class 기본_설정 {

        @Autowired
        ApplicationContext context;

        @Test
        void Embedding과_Vector_검색_빈이_없다() {
            assertThat(context.getBeanNamesForType(EmbeddingModel.class)).isEmpty();
            assertThat(context.getBeanNamesForType(StudyPostVectorService.class)).isEmpty();
        }
    }

    @Nested
    @SpringBootTest(properties = {
            "app.vector.enabled=true",
            "app.vector.qdrant-host=localhost",
            "app.vector.qdrant-grpc-port=6334",
            "app.vector.collection=study_posts",
            "app.vector.dimensions=768",
            "app.vector.embedding-model=gemini-embedding-001",
            "app.vector.gemini-api-key=test-key"
    })
    class Vector_검색_활성화 {

        @Autowired
        ApplicationContext context;

        @Test
        void Gemini_Embedding과_Vector_검색_빈이_만들어진다() {
            assertThat(context.getBeanNamesForType(EmbeddingModel.class)).hasSize(1);
            assertThat(context.getBeanNamesForType(StudyPostVectorService.class)).hasSize(1);
            assertThat(context.getBeanNamesForType(VectorSearchController.class)).hasSize(1);
            assertThat(context.getBean(VectorSearchProps.class).toString()).doesNotContain("test-key");
        }
    }
}
