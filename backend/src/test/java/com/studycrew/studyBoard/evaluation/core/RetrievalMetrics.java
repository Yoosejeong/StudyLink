package com.studycrew.studyBoard.evaluation.core;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class RetrievalMetrics {

    private RetrievalMetrics() {}

    public static double recallAtK(List<Long> ranked, Map<Long, Integer> relevance, int k) {
        long relevant = relevantCount(relevance);
        long hits = topK(ranked, k).stream().filter(id -> relevance.getOrDefault(id, 0) > 0).count();
        return (double) hits / relevant;
    }

    // gain = 2^rel - 1, discount = log2(rank + 1)
    public static double ndcgAtK(List<Long> ranked, Map<Long, Integer> relevance, int k) {
        relevantCount(relevance);
        List<Long> top = topK(ranked, k);
        double dcg = 0;
        for (int i = 0; i < top.size(); i++) {
            dcg += gain(relevance.getOrDefault(top.get(i), 0)) / log2(i + 2);
        }
        List<Integer> ideal = relevance.values().stream()
                .filter(r -> r > 0)
                .sorted(Comparator.reverseOrder())
                .limit(k)
                .toList();
        double idcg = 0;
        for (int i = 0; i < ideal.size(); i++) {
            idcg += gain(ideal.get(i)) / log2(i + 2);
        }
        return dcg / idcg;
    }

    private static long relevantCount(Map<Long, Integer> relevance) {
        long count = relevance.values().stream().filter(r -> r > 0).count();
        if (count == 0) {
            throw new IllegalArgumentException("정답이 없는 질문은 Recall/nDCG를 계산할 수 없습니다.");
        }
        return count;
    }

    private static List<Long> topK(List<Long> ranked, int k) {
        return ranked.stream().distinct().limit(k).toList();
    }

    private static double gain(int rel) {
        return rel <= 0 ? 0 : Math.pow(2, rel) - 1;
    }

    private static double log2(int x) {
        return Math.log(x) / Math.log(2);
    }
}
