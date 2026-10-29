package com.studycrew.studyBoard.evaluation.core;

import java.util.List;

/**
 * FULLTEXT / Vector / Hybrid 검색은 이 인터페이스만 구현하면 같은 평가 코드를 사용할 수 있다.
 */
public interface SearchStrategy {

    String name();

    List<Long> search(EvalQuery query, int k);

    static SearchStrategy of(String name, Searcher searcher) {
        return new SearchStrategy() {
            @Override public String name() { return name; }
            @Override public List<Long> search(EvalQuery query, int k) { return searcher.search(query, k); }
        };
    }

    @FunctionalInterface
    interface Searcher {
        List<Long> search(EvalQuery query, int k);
    }
}
