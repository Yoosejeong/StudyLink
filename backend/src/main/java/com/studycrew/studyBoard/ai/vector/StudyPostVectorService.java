package com.studycrew.studyBoard.ai.vector;

import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.vector", name = "enabled", havingValue = "true")
public class StudyPostVectorService {

    // Gemini batch 임베딩 요청 1회당 최대 입력 개수
    static final int EMBEDDING_BATCH_SIZE = 100;

    private final StudyPostRepository studyPostRepository;
    private final GeminiEmbedder geminiEmbedder;
    private final StudyPostVectorStore vectorStore;
    private final VectorSearchProps props;

    public IndexResult indexAll() {
        long start = System.currentTimeMillis();
        vectorStore.ensureCollection();

        List<StudyPost> posts = studyPostRepository.findAllByDeletedFalseOrderByIdAsc();
        for (int from = 0; from < posts.size(); from += EMBEDDING_BATCH_SIZE) {
            List<StudyPost> batch = posts.subList(from, Math.min(from + EMBEDDING_BATCH_SIZE, posts.size()));
            List<float[]> vectors = geminiEmbedder.embedDocuments(batch.stream().map(StudyPostEmbeddingText::of).toList());
            vectorStore.upsert(batch, vectors);
            log.info("모집글 임베딩 저장: {}/{}", from + batch.size(), posts.size());
        }

        IndexResult result = new IndexResult(props.collection(), posts.size(), vectorStore.count(),
                System.currentTimeMillis() - start);
        log.info("모집글 임베딩 완료: {}", result);
        return result;
    }

    public List<VectorSearchResult> search(String query, int topK) {
        return vectorStore.search(geminiEmbedder.embedQuery(query), topK);
    }

    public record IndexResult(String collection, int indexedPosts, long totalPoints, long elapsedMs) {}
}
