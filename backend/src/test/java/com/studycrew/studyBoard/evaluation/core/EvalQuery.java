package com.studycrew.studyBoard.evaluation.core;

import java.util.Map;

/**
 * @param query     자연어 질문 (Vector 검색 입력)
 * @param keyword   핵심 키워드 (FULLTEXT 검색 입력)
 * @param relevance 모집글 ID → 관련도 (2: 정답, 1: 부분 정답)
 */
public record EvalQuery(String id, String type, String query, String keyword, Map<Long, Integer> relevance) {

    public EvalQuery {
        relevance = relevance == null ? Map.of() : Map.copyOf(relevance);
    }

    public boolean expectsNoResult() {
        return relevance.values().stream().noneMatch(r -> r > 0);
    }
}
