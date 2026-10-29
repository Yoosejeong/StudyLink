package com.studycrew.studyBoard.ai.vector;

import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.Category;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.JsonWithInt.Value;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.qdrant.client.PointIdFactory.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Gemini 대신 키워드 기반 가짜 임베딩으로 Qdrant 저장/검색 흐름을 검증한다.
 * 로컬 Qdrant(docker compose --profile vector up -d qdrant)가 없으면 건너뛴다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudyPostVectorServiceQdrantTest {

    private static final String COLLECTION = "study_posts_test";
    private static final List<String> KEYWORDS = List.of("React", "Spring", "클라우드", "알고리즘", "모바일");
    private static final int DIMENSIONS = KEYWORDS.size() + 1;

    private final AtomicInteger embeddingCalls = new AtomicInteger();
    private QdrantClient qdrantClient;
    private StudyPostRepository studyPostRepository;
    private StudyPostVectorService service;

    @BeforeAll
    void connect() {
        qdrantClient = new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build());
        assumeTrue(qdrantAvailable(), "로컬 Qdrant(localhost:6334)가 실행 중이 아니어서 건너뜁니다.");
    }

    @BeforeEach
    void setUp() throws Exception {
        if (qdrantClient.collectionExistsAsync(COLLECTION).get()) {
            qdrantClient.deleteCollectionAsync(COLLECTION).get();
        }
        VectorSearchProps props = new VectorSearchProps(true, "localhost", 6334, COLLECTION, DIMENSIONS,
                "test-model", "test-key");
        studyPostRepository = mock(StudyPostRepository.class);
        when(studyPostRepository.findAllByDeletedFalseOrderByIdAsc()).thenReturn(List.of(
                post(1L, "백엔드 스터디", "Spring Boot 핵심 개념", Category.BACKEND),
                post(2L, "프론트엔드 협업", "React 기반 프로젝트", Category.FRONTEND),
                post(3L, "AWS 인프라", "클라우드 인프라 구성", Category.CLOUD_DEVOPS),
                post(4L, "코딩테스트 집중반", "알고리즘 문제 풀이", Category.ALGORITHM_CS)));
        embeddingCalls.set(0);
        service = new StudyPostVectorService(studyPostRepository,
                new GeminiEmbedder(new KeywordEmbeddingModel(), props),
                new StudyPostVectorStore(qdrantClient, props), props);
    }

    @AfterAll
    void tearDown() throws Exception {
        if (qdrantClient == null) return;
        if (qdrantAvailable() && qdrantClient.collectionExistsAsync(COLLECTION).get()) {
            qdrantClient.deleteCollectionAsync(COLLECTION).get();
        }
        qdrantClient.close();
    }

    @Test
    void 컬렉션을_만들고_모집글을_한_번의_임베딩_요청으로_저장한다() {
        StudyPostVectorService.IndexResult result = service.indexAll();

        assertThat(result.indexedPosts()).isEqualTo(4);
        assertThat(result.totalPoints()).isEqualTo(4);
        assertThat(embeddingCalls.get()).isEqualTo(1);
    }

    @Test
    void 다시_실행해도_같은_모집글은_중복_저장되지_않는다() {
        service.indexAll();
        StudyPostVectorService.IndexResult second = service.indexAll();

        assertThat(second.totalPoints()).isEqualTo(4);
    }

    @Test
    void 모집글_ID를_point_ID로_쓰고_상태와_카테고리를_payload로_저장한다() throws Exception {
        service.indexAll();

        Map<String, Value> payload = qdrantClient.retrieveAsync(COLLECTION, List.of(id(3L)), true, false, null)
                .get().get(0).getPayloadMap();

        assertThat(payload.get("studyPostId").getIntegerValue()).isEqualTo(3L);
        assertThat(payload.get("studyStatus").getStringValue()).isEqualTo("RECRUITING");
        assertThat(payload.get("category").getStringValue()).isEqualTo("CLOUD_DEVOPS");
    }

    @Test
    void 질문과_가장_가까운_모집글_ID와_점수를_반환한다() {
        service.indexAll();

        List<VectorSearchResult> results = service.search("React 프로젝트 하고 싶어", 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).studyPostId()).isEqualTo(2L);
        assertThat(results.get(0).score()).isGreaterThan(results.get(1).score());
    }

    @Test
    void 기존_컬렉션의_차원이_다르면_저장하지_않고_실패한다() throws Exception {
        qdrantClient.createCollectionAsync(COLLECTION,
                VectorParams.newBuilder().setSize(DIMENSIONS + 1).setDistance(Distance.Cosine).build()).get();

        assertThatThrownBy(() -> service.indexAll())
                .isInstanceOf(VectorSearchException.class)
                .hasMessageContaining("차원");
        assertThat(embeddingCalls.get()).isZero();
    }

    private boolean qdrantAvailable() {
        try {
            qdrantClient.collectionExistsAsync(COLLECTION, Duration.ofSeconds(2)).get();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private StudyPost post(Long id, String title, String content, Category category) {
        return StudyPost.builder().id(id).title(title).content(content).category(category)
                .studyStatus(StudyStatus.RECRUITING).build();
    }

    // 텍스트에 포함된 키워드 위치를 1로 채우는 가짜 임베딩 (마지막 차원은 0 벡터 방지용)
    private class KeywordEmbeddingModel implements EmbeddingModel {

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            embeddingCalls.incrementAndGet();
            List<Embedding> embeddings = new ArrayList<>();
            List<String> texts = request.getInstructions();
            for (int i = 0; i < texts.size(); i++) {
                embeddings.add(new Embedding(vector(texts.get(i)), i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return vector(document.getText());
        }

        private float[] vector(String text) {
            float[] vector = new float[DIMENSIONS];
            IntStream.range(0, KEYWORDS.size()).filter(i -> text.contains(KEYWORDS.get(i))).forEach(i -> vector[i] = 1f);
            vector[DIMENSIONS - 1] = 0.1f;
            return vector;
        }
    }
}
