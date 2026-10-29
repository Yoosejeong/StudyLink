package com.studycrew.studyBoard.ai.vector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions.TaskType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Gemini는 저장할 문서(RETRIEVAL_DOCUMENT)와 검색 질문(RETRIEVAL_QUERY)의 task type을 구분해야
 * 검색 품질이 좋아서, 같은 옵션을 쓰는 Spring AI VectorStore 대신 EmbeddingModel을 직접 호출한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.vector", name = "enabled", havingValue = "true")
public class GeminiEmbedder {

    private final EmbeddingModel embeddingModel;
    private final VectorSearchProps props;

    public List<float[]> embedDocuments(List<String> texts) {
        return embed(texts, TaskType.RETRIEVAL_DOCUMENT);
    }

    public float[] embedQuery(String query) {
        return embed(List.of(query), TaskType.RETRIEVAL_QUERY).get(0);
    }

    private List<float[]> embed(List<String> texts, TaskType taskType) {
        GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
                .taskType(taskType)
                .dimensions(props.dimensions())
                .build();
        List<float[]> vectors;
        try {
            vectors = embeddingModel.call(new EmbeddingRequest(texts, options)).getResults().stream()
                    .map(Embedding::getOutput)
                    .toList();
        } catch (RuntimeException e) {
            log.error("Gemini 임베딩 실패: taskType={}, inputs={}, cause={}", taskType, texts.size(), e.toString());
            throw new VectorSearchException("Gemini 임베딩 호출에 실패했습니다.", e);
        }

        if (vectors.size() != texts.size()) {
            throw new VectorSearchException("임베딩 결과 개수가 입력과 다릅니다: inputs=%d, outputs=%d"
                    .formatted(texts.size(), vectors.size()));
        }
        vectors.stream().filter(v -> v.length != props.dimensions()).findAny().ifPresent(v -> {
            throw new VectorSearchException("임베딩 차원이 설정과 다릅니다: expected=%d, actual=%d"
                    .formatted(props.dimensions(), v.length));
        });
        return vectors;
    }
}
