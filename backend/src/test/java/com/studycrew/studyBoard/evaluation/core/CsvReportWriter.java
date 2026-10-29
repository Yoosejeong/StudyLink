package com.studycrew.studyBoard.evaluation.core;

import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.EvaluationRun;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.QueryResult;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.Summary;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CsvReportWriter {

    // 엑셀에서 한글이 깨지지 않도록 UTF-8 BOM을 붙인다
    private static final String BOM = "﻿";

    static final List<String> QUERY_HEADER = List.of(
            "strategy", "k", "query_id", "type", "query", "keyword", "relevant_ids", "result_ids",
            "hits", "recall_at_k", "ndcg_at_k", "no_result_correct",
            "latency_mean_ms", "latency_p50_ms", "latency_p95_ms", "latency_max_ms");

    static final List<String> SUMMARY_HEADER = List.of(
            "strategy", "k", "group", "queries", "scored_queries", "mean_recall_at_k", "mean_ndcg_at_k",
            "no_result_queries", "no_result_correct",
            "latency_samples", "latency_mean_ms", "latency_p50_ms", "latency_p95_ms", "latency_max_ms",
            "measured_at");

    private CsvReportWriter() {}

    public static Path writeQueryResults(EvaluationRun run, Path dir) {
        Stream<List<String>> rows = run.results().stream().map(r -> queryRow(run, r));
        return write(dir.resolve(run.strategyName() + "-queries.csv"), QUERY_HEADER, rows);
    }

    public static Path writeSummary(List<EvaluationRun> runs, Path dir, String fileName) {
        Stream<List<String>> rows = runs.stream().flatMap(run -> run.summaries().stream()).map(CsvReportWriter::summaryRow);
        return write(dir.resolve(fileName), SUMMARY_HEADER, rows);
    }

    private static List<String> queryRow(EvaluationRun run, QueryResult r) {
        Map<Long, Integer> relevance = r.query().relevance();
        long hits = r.resultIds().stream().filter(id -> relevance.getOrDefault(id, 0) > 0).count();
        LatencyStats latency = r.latency();
        return List.of(run.strategyName(), String.valueOf(run.k()), r.query().id(), r.query().type(),
                r.query().query(), r.query().keyword(), relevantIds(relevance), joinIds(r.resultIds()),
                String.valueOf(hits), number(r.recall()), number(r.ndcg()),
                r.noResultCorrect() == null ? "" : r.noResultCorrect().toString(),
                number(latency.meanMs()), number(latency.p50Ms()), number(latency.p95Ms()), number(latency.maxMs()));
    }

    private static List<String> summaryRow(Summary s) {
        LatencyStats latency = s.latency();
        return List.of(s.strategyName(), String.valueOf(s.k()), s.group(), String.valueOf(s.queries()),
                String.valueOf(s.scoredQueries()), number(s.meanRecall()), number(s.meanNdcg()),
                String.valueOf(s.noResultQueries()), String.valueOf(s.noResultCorrect()),
                String.valueOf(latency.samples()), number(latency.meanMs()), number(latency.p50Ms()),
                number(latency.p95Ms()), number(latency.maxMs()), s.measuredAt().toString());
    }

    private static String relevantIds(Map<Long, Integer> relevance) {
        return relevance.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(";"));
    }

    private static String joinIds(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(";"));
    }

    private static String number(Double value) {
        return value == null ? "" : String.format(Locale.ROOT, "%.4f", value);
    }

    static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static Path write(Path path, List<String> header, Stream<List<String>> rows) {
        String body = Stream.concat(Stream.of(header), rows)
                .map(row -> row.stream().map(CsvReportWriter::escape).collect(Collectors.joining(",")))
                .collect(Collectors.joining("\n", BOM, "\n"));
        try {
            Files.createDirectories(path.toAbsolutePath().getParent());
            return Files.writeString(path, body, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV를 저장할 수 없습니다: " + path, e);
        }
    }
}
