package com.studycrew.studyBoard.ai.vector;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

/**
 * Spring AI의 Gemini Embedding 자동 설정은 API 키가 없는 환경(운영, 테스트)에서 앱 시작을 실패시키므로
 * StudyBoardApplication에서 제외하고, Vector 검색을 켠 환경(local)에서만 직접 생성한다.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.vector", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(VectorSearchProps.class)
public class VectorSearchConfig {

    @Bean(destroyMethod = "close")
    public QdrantClient qdrantClient(VectorSearchProps props) {
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(props.qdrantHost(), props.qdrantGrpcPort(), false).build());
    }

    @Bean
    public EmbeddingModel geminiEmbeddingModel(VectorSearchProps props) {
        GoogleGenAiEmbeddingConnectionDetails connection = GoogleGenAiEmbeddingConnectionDetails.builder()
                .apiKey(props.geminiApiKey())
                .build();
        GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
                .model(props.embeddingModel())
                .dimensions(props.dimensions())
                .build();
        // 무료 호출량을 아끼기 위해 실패 시 1번만 재시도 (Spring AI 기본값은 10회)
        RetryTemplate retryTemplate = RetryTemplate.builder().maxAttempts(2).fixedBackoff(2000).build();
        return new GoogleGenAiTextEmbeddingModel(connection, options, retryTemplate);
    }
}
