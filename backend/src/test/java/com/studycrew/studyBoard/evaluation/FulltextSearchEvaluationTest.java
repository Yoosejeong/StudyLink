package com.studycrew.studyBoard.evaluation;

import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
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
        SearchStrategy fulltext = SearchStrategy.of("fulltext_title_content", (query, k) -> studyPostQueryService
                .getStudyPostList(query.keyword(), StudyStatus.RECRUITING, null, null, k)
                .getItems().stream()
                .map(GetStudyPostListResponse::getStudyPostId)
                .toList());

        EvaluationRun run = new SearchEvaluator(K, WARMUP_ROUNDS, MEASURED_ROUNDS).evaluate(fulltext, queries);

        // 제목 전용 검색과 비교한 fulltext-summary.csv 는 기록으로 남기고 덮어쓰지 않는다
        Path outputDir = CsvReportWriter.defaultOutputDir();
        CsvReportWriter.writeQueryResults(run, outputDir);
        CsvReportWriter.writeSummary(List.of(run), outputDir, "fulltext_title_content-summary.csv");
        run.summaries().stream().map(Summary::toConsoleLine).forEach(System.out::println);
        System.out.println("CSV 저장 위치: " + outputDir.toAbsolutePath());

        assertThat(run.results()).hasSize(queries.size());
    }
}
