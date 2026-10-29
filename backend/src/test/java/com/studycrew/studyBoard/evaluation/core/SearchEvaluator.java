package com.studycrew.studyBoard.evaluation.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부 API(예: Gemini Embedding)를 호출하는 전략은 호출 비용을 고려해 반복 횟수를 줄여서 사용한다.
 */
public class SearchEvaluator {

    private final int k;
    private final int warmupRounds;
    private final int measuredRounds;

    public SearchEvaluator(int k, int warmupRounds, int measuredRounds) {
        if (k < 1 || warmupRounds < 0 || measuredRounds < 1) {
            throw new IllegalArgumentException("k, measuredRounds는 1 이상, warmupRounds는 0 이상이어야 합니다.");
        }
        this.k = k;
        this.warmupRounds = warmupRounds;
        this.measuredRounds = measuredRounds;
    }

    public EvaluationRun evaluate(SearchStrategy strategy, List<EvalQuery> queries) {
        List<QueryResult> results = new ArrayList<>();
        for (EvalQuery query : queries) {
            for (int i = 0; i < warmupRounds; i++) {
                strategy.search(query, k);
            }
            List<Long> nanos = new ArrayList<>();
            List<Long> ids = List.of();
            for (int i = 0; i < measuredRounds; i++) {
                long start = System.nanoTime();
                ids = strategy.search(query, k);
                nanos.add(System.nanoTime() - start);
            }
            results.add(QueryResult.of(query, ids.stream().limit(k).toList(), k, nanos));
        }
        return new EvaluationRun(strategy.name(), k, LocalDateTime.now(), results);
    }

    public record QueryResult(EvalQuery query, List<Long> resultIds, Double recall, Double ndcg,
                              Boolean noResultCorrect, List<Long> latencyNanos) {

        static QueryResult of(EvalQuery query, List<Long> ids, int k, List<Long> nanos) {
            if (query.expectsNoResult()) {
                return new QueryResult(query, ids, null, null, ids.isEmpty(), nanos);
            }
            return new QueryResult(query, ids,
                    RetrievalMetrics.recallAtK(ids, query.relevance(), k),
                    RetrievalMetrics.ndcgAtK(ids, query.relevance(), k),
                    null, nanos);
        }

        public LatencyStats latency() {
            return LatencyStats.fromNanos(latencyNanos);
        }
    }

    public record EvaluationRun(String strategyName, int k, LocalDateTime measuredAt, List<QueryResult> results) {

        public List<Summary> summaries() {
            Map<String, List<QueryResult>> groups = new LinkedHashMap<>();
            groups.put("ALL", results);
            results.forEach(r -> groups.computeIfAbsent(r.query().type(), t -> new ArrayList<>()).add(r));

            return groups.entrySet().stream().map(e -> Summary.of(this, e.getKey(), e.getValue())).toList();
        }
    }

    public record Summary(String strategyName, int k, String group, int queries, int scoredQueries,
                          Double meanRecall, Double meanNdcg, int noResultQueries, int noResultCorrect,
                          LatencyStats latency, LocalDateTime measuredAt) {

        static Summary of(EvaluationRun run, String group, List<QueryResult> results) {
            List<QueryResult> scored = results.stream().filter(r -> r.recall() != null).toList();
            List<QueryResult> noResult = results.stream().filter(r -> r.noResultCorrect() != null).toList();
            List<Long> nanos = results.stream().flatMap(r -> r.latencyNanos().stream()).toList();
            return new Summary(run.strategyName(), run.k(), group, results.size(), scored.size(),
                    scored.isEmpty() ? null : scored.stream().mapToDouble(QueryResult::recall).average().orElseThrow(),
                    scored.isEmpty() ? null : scored.stream().mapToDouble(QueryResult::ndcg).average().orElseThrow(),
                    noResult.size(), (int) noResult.stream().filter(QueryResult::noResultCorrect).count(),
                    LatencyStats.fromNanos(nanos), run.measuredAt());
        }
    }
}
