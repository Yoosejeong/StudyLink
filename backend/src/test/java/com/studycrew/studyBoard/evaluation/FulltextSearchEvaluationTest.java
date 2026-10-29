package com.studycrew.studyBoard.evaluation;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.enums.SearchTarget;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.evaluation.core.CsvReportWriter;
import com.studycrew.studyBoard.evaluation.core.EvalQuery;
import com.studycrew.studyBoard.evaluation.core.EvaluationDataset;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.EvaluationRun;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.Summary;
import com.studycrew.studyBoard.evaluation.core.SearchStrategy;
import com.studycrew.studyBoard.service.studyPost.StudyPostQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 로컬 개발 DB(studycrewdb)의 평가용 모집글 30건과 FULLTEXT 인덱스에 의존하므로 SEARCH_EVAL=true 일 때만 실행한다.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/studycrewdb"
                + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false",                   // SQL 로그 출력이 지연시간에 섞이지 않도록
        "spring.jpa.properties.hibernate.format_sql=false"
})
@EnabledIfEnvironmentVariable(named = "SEARCH_EVAL", matches = "true")
class FulltextSearchEvaluationTest {

    private static final int K = 5;
    private static final int WARMUP_ROUNDS = 3;
    private static final int MEASURED_ROUNDS = 20;

    @Autowired
    StudyPostQueryService studyPostQueryService;

    @Test
    void fulltextBaseline() {
        List<EvalQuery> queries = EvaluationDataset.load(EvaluationDataset.DEFAULT_PATH);
        SearchEvaluator evaluator = new SearchEvaluator(K, WARMUP_ROUNDS, MEASURED_ROUNDS);

        List<EvaluationRun> runs = List.of(
                evaluator.evaluate(fulltext("fulltext_title_content", SearchTarget.TITLE_CONTENT), queries),
                evaluator.evaluate(fulltext("fulltext_title", SearchTarget.TITLE), queries));

        Path outputDir = outputDir();
        runs.forEach(run -> CsvReportWriter.writeQueryResults(run, outputDir));
        CsvReportWriter.writeSummary(runs, outputDir, "fulltext-summary.csv");

        runs.forEach(run -> run.summaries().forEach(this::print));
        System.out.println("CSV 저장 위치: " + outputDir.toAbsolutePath());

        assertThat(runs).allSatisfy(run -> assertThat(run.results()).hasSize(queries.size()));
    }

    private SearchStrategy fulltext(String name, SearchTarget target) {
        return SearchStrategy.of(name, (query, k) -> studyPostQueryService
                .getStudyPostList(target, query.keyword(), StudyStatus.RECRUITING, null, null, k)
                .getItems().stream()
                .map(GetStudyPostListResponse::getStudyPostId)
                .toList());
    }

    private Path outputDir() {
        String dir = System.getenv("SEARCH_EVAL_OUTPUT_DIR");
        return Path.of(dir == null || dir.isBlank() ? "evaluation-results" : dir);
    }

    private void print(Summary s) {
        System.out.println(String.format(Locale.ROOT,
                "[%s] %-9s queries=%2d recall@%d=%s ndcg@%d=%s noResult=%d/%d latency p50=%.2fms p95=%.2fms",
                s.strategyName(), s.group(), s.queries(), s.k(), format(s.meanRecall()), s.k(), format(s.meanNdcg()),
                s.noResultCorrect(), s.noResultQueries(), s.latency().p50Ms(), s.latency().p95Ms()));
    }

    private String format(Double value) {
        return value == null ? "-" : String.format(Locale.ROOT, "%.3f", value);
    }
}
