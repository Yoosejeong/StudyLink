package com.studycrew.studyBoard.ai.vector;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.vector")
public record VectorSearchProps(boolean enabled, String qdrantHost, int qdrantGrpcPort, String collection,
                                int dimensions, String embeddingModel, String geminiApiKey) {

    // 설정 객체가 로그에 찍혀도 API 키가 노출되지 않도록
    @Override
    public String toString() {
        return "VectorSearchProps[enabled=%s, qdrantHost=%s, qdrantGrpcPort=%d, collection=%s, dimensions=%d, embeddingModel=%s]"
                .formatted(enabled, qdrantHost, qdrantGrpcPort, collection, dimensions, embeddingModel);
    }
}
