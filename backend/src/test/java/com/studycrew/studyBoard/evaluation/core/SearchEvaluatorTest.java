package com.studycrew.studyBoard.evaluation.core;

import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.EvaluationRun;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.QueryResult;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.Summary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SearchEvaluatorTest {

    private static final List<EvalQuery> QUERIES = List.of(
            new EvalQuery("Q1", "EXACT", "백엔드, 스터디 찾아줘", "백엔드", Map.of(1L, 2, 2L, 1)),
            new EvalQuery("Q2", "CONTENT", "React 하고 싶어", "React", Map.of(3L, 2)),
            new EvalQuery("Q3", "NO_RESULT", "Rust", "Rust", Map.of()));

    private static final Map<String, List<Long>> ANSWERS = Map.of(
            "Q1", List.of(2L, 1L, 9L, 8L, 7L, 6L),  // k=5 초과분은 잘려야 함
            "Q2", List.of(),
            "Q3", List.of());

    @Test
    void 질문마다_워밍업과_측정_횟수만큼_검색한다() {
        Map<String, Integer> calls = new HashMap<>();
        SearchStrategy counting = SearchStrategy.of("fake", (q, k) -> {
            calls.merge(q.id(), 1, Integer::sum);
            return ANSWERS.get(q.id());
        });

        EvaluationRun run = new SearchEvaluator(5, 2, 3).evaluate(counting, QUERIES);

        assertThat(calls).containsExactlyInAnyOrderEntriesOf(Map.of("Q1", 5, "Q2", 5, "Q3", 5));
        assertThat(run.results()).allSatisfy(r -> assertThat(r.latencyNanos()).hasSize(3));
    }

    @Test
    void 질문별_지표와_결과없음_정답여부를_계산한다() {
        EvaluationRun run = new SearchEvaluator(5, 0, 1).evaluate(fake(), QUERIES);

        QueryResult q1 = run.results().get(0);
        assertThat(q1.resultIds()).containsExactly(2L, 1L, 9L, 8L, 7L);
        assertThat(q1.recall()).isEqualTo(1.0);
        assertThat(q1.ndcg()).isCloseTo(0.796707, within(1e-6));
        assertThat(q1.noResultCorrect()).isNull();

        QueryResult q3 = run.results().get(2);
        assertThat(q3.recall()).isNull();
        assertThat(q3.noResultCorrect()).isTrue();
    }

    @Test
    void 전체와_유형별_요약을_만든다() {
        List<Summary> summaries = new SearchEvaluator(5, 0, 1).evaluate(fake(), QUERIES).summaries();

        assertThat(summaries).extracting(Summary::group).containsExactly("ALL", "EXACT", "CONTENT", "NO_RESULT");
        Summary all = summaries.get(0);
        assertThat(all.queries()).isEqualTo(3);
        assertThat(all.scoredQueries()).isEqualTo(2);
        assertThat(all.meanRecall()).isEqualTo(0.5);
        assertThat(all.noResultCorrect()).isEqualTo(1);
        assertThat(summaries.get(3).meanRecall()).isNull();
    }

    @Test
    void 지연시간_백분위를_계산한다() {
        List<Long> nanos = LongStream.rangeClosed(1, 20).map(ms -> ms * 1_000_000).boxed().toList();

        LatencyStats stats = LatencyStats.fromNanos(nanos);

        assertThat(stats.samples()).isEqualTo(20);
        assertThat(stats.meanMs()).isEqualTo(10.5);
        assertThat(stats.p50Ms()).isEqualTo(10.0);
        assertThat(stats.p95Ms()).isEqualTo(19.0);
        assertThat(stats.maxMs()).isEqualTo(20.0);
    }

    @Test
    void CSV로_질문별_결과와_요약을_저장한다(@TempDir Path dir) throws IOException {
        EvaluationRun run = new SearchEvaluator(5, 0, 1).evaluate(fake(), QUERIES);

        Path queries = CsvReportWriter.writeQueryResults(run, dir);
        Path summary = CsvReportWriter.writeSummary(List.of(run), dir, "summary.csv");

        List<String> queryLines = Files.readAllLines(queries, StandardCharsets.UTF_8);
        assertThat(queries.getFileName().toString()).isEqualTo("fake-queries.csv");
        assertThat(queryLines.get(0)).startsWith("﻿strategy,k,query_id");
        assertThat(queryLines).hasSize(1 + QUERIES.size());
        assertThat(queryLines.get(1))
                .contains("\"백엔드, 스터디 찾아줘\"")
                .contains(",1:2;2:1,2;1;9;8;7,2,1.0000,0.7967,,");
        assertThat(queryLines.get(3)).contains(",Rust,Rust,,,0,,,true,");

        assertThat(Files.readAllLines(summary, StandardCharsets.UTF_8)).hasSize(1 + 4);
    }

    @Test
    void CSV_특수문자를_이스케이프한다() {
        assertThat(CsvReportWriter.escape("a\"b")).isEqualTo("\"a\"\"b\"");
        assertThat(CsvReportWriter.escape("plain")).isEqualTo("plain");
        assertThat(CsvReportWriter.escape(null)).isEmpty();
    }

    private SearchStrategy fake() {
        return SearchStrategy.of("fake", (q, k) -> ANSWERS.get(q.id()));
    }
}
