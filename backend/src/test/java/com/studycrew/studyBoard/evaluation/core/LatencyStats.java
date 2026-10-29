package com.studycrew.studyBoard.evaluation.core;

import java.util.List;

public record LatencyStats(int samples, double meanMs, double p50Ms, double p95Ms, double maxMs) {

    public static LatencyStats fromNanos(List<Long> nanos) {
        if (nanos.isEmpty()) {
            throw new IllegalArgumentException("지연시간 샘플이 없습니다.");
        }
        List<Long> sorted = nanos.stream().sorted().toList();
        double mean = sorted.stream().mapToLong(Long::longValue).average().orElseThrow();
        return new LatencyStats(sorted.size(), toMs(mean), toMs(percentile(sorted, 50)),
                toMs(percentile(sorted, 95)), toMs(sorted.get(sorted.size() - 1)));
    }

    static long percentile(List<Long> sorted, int p) {
        int rank = (int) Math.ceil(p / 100.0 * sorted.size());
        return sorted.get(Math.max(rank, 1) - 1);
    }

    private static double toMs(double nanos) {
        return nanos / 1_000_000.0;
    }
}
