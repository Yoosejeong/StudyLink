package com.studycrew.studyBoard.evaluation;

import com.studycrew.studyBoard.ai.vector.StudyPostVectorService;
import com.studycrew.studyBoard.ai.vector.StudyPostVectorStore;
import com.studycrew.studyBoard.ai.vector.VectorSearchResult;
import com.studycrew.studyBoard.evaluation.core.CsvReportWriter;
import com.studycrew.studyBoard.evaluation.core.EvalQuery;
import com.studycrew.studyBoard.evaluation.core.EvaluationDataset;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.EvaluationRun;
import com.studycrew.studyBoard.evaluation.core.SearchEvaluator.Summary;
import com.studycrew.studyBoard.evaluation.core.SearchStrategy;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 로컬 Qdrant에 저장된 모집글 벡터(POST /api/ai/vector/index)를 Gemini로 검색해 FULLTEXT 기준점과 같은 방식으로 평가한다.
 * 질문마다 Gemini Embedding을 호출하므로 SEARCH_EVAL=true, GEMINI_API_KEY가 있을 때만 실행한다.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/studycrewdb"
                + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.format_sql=false",
        "app.vector.enabled=true",
        "app.vector.qdrant-host=localhost",
        "app.vector.qdrant-grpc-port=6334",
        "app.vector.collection=study_posts",
        "app.vector.dimensions=768",
        "app.vector.embedding-model=gemini-embedding-001",
        "app.vector.gemini-api-key=${GEMINI_API_KEY}"
})
@EnabledIfEnvironmentVariable(named = "SEARCH_EVAL", matches = "true")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class VectorSearchEvaluationTest {

    private static final int K = 5;
    // Gemini 무료 호출량을 아끼기 위해 질문당 1회만 호출 (지연시간은 Gemini 호출 포함 단일 측정값)
    private static final int WARMUP_ROUNDS = 0;
    private static final int MEASURED_ROUNDS = 1;

    @Autowired
    StudyPostVectorService studyPostVectorService;
    @Autowired
    StudyPostVectorStore studyPostVectorStore;
    @Autowired
    StudyPostRepository studyPostRepository;

    @Test
    void vectorSearch() {
        assertThat(studyPostVectorStore.count())
                .as("Qdrant에 모집글 벡터가 없습니다. 먼저 POST /api/ai/vector/index 를 호출하세요.")
                .isEqualTo(studyPostRepository.findAllByDeletedFalseOrderByIdAsc().size());

        List<EvalQuery> queries = EvaluationDataset.load(EvaluationDataset.DEFAULT_PATH);
        SearchStrategy vector = SearchStrategy.of("vector", (query, k) -> studyPostVectorService
                .search(query.query(), k).stream()
                .map(VectorSearchResult::studyPostId)
                .toList());

        EvaluationRun run = new SearchEvaluator(K, WARMUP_ROUNDS, MEASURED_ROUNDS).evaluate(vector, queries);

        Path outputDir = CsvReportWriter.defaultOutputDir();
        CsvReportWriter.writeQueryResults(run, outputDir);
        CsvReportWriter.writeSummary(List.of(run), outputDir, "vector-summary.csv");
        run.summaries().stream().map(Summary::toConsoleLine).forEach(System.out::println);
        System.out.println("CSV 저장 위치: " + outputDir.toAbsolutePath());

        assertThat(run.results()).hasSize(queries.size());
    }
}
