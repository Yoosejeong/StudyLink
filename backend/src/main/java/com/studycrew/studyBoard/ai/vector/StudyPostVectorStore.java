package com.studycrew.studyBoard.ai.vector;

import com.google.common.util.concurrent.ListenableFuture;
import com.studycrew.studyBoard.entity.StudyPost;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.UpsertPoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.vector", name = "enabled", havingValue = "true")
public class StudyPostVectorStore {

    private final QdrantClient qdrantClient;
    private final VectorSearchProps props;

    public void ensureCollection() {
        String collection = props.collection();
        if (await(qdrantClient.collectionExistsAsync(collection), "컬렉션 확인")) {
            long size = await(qdrantClient.getCollectionInfoAsync(collection), "컬렉션 조회")
                    .getConfig().getParams().getVectorsConfig().getParams().getSize();
            if (size != props.dimensions()) {
                throw new VectorSearchException("Qdrant 컬렉션 차원이 설정과 다릅니다: collection=%s, expected=%d, actual=%d"
                        .formatted(collection, props.dimensions(), size));
            }
            return;
        }
        VectorParams params = VectorParams.newBuilder()
                .setSize(props.dimensions())
                .setDistance(Distance.Cosine)
                .build();
        await(qdrantClient.createCollectionAsync(collection, params), "컬렉션 생성");
        log.info("Qdrant 컬렉션 생성: collection={}, dimensions={}", collection, props.dimensions());
    }

    public void upsert(List<StudyPost> posts, List<float[]> vectors) {
        List<PointStruct> points = IntStream.range(0, posts.size())
                .mapToObj(i -> toPoint(posts.get(i), vectors.get(i)))
                .toList();
        UpsertPoints request = UpsertPoints.newBuilder()
                .setCollectionName(props.collection())
                .setWait(true)
                .addAllPoints(points)
                .build();
        await(qdrantClient.upsertAsync(request), "벡터 저장");
    }

    public List<VectorSearchResult> search(float[] queryVector, int topK) {
        QueryPoints request = QueryPoints.newBuilder()
                .setCollectionName(props.collection())
                .setQuery(nearest(queryVector))
                .setLimit(topK)
                .setWithPayload(enable(false))
                .build();
        return await(qdrantClient.queryAsync(request), "벡터 검색").stream()
                .map(point -> new VectorSearchResult(point.getId().getNum(), point.getScore()))
                .toList();
    }

    public long count() {
        return await(qdrantClient.countAsync(props.collection()), "개수 조회");
    }

    // 모집글 ID를 point ID로 사용해 다시 실행해도 같은 point를 덮어쓴다
    private PointStruct toPoint(StudyPost post, float[] vector) {
        return PointStruct.newBuilder()
                .setId(id(post.getId()))
                .setVectors(vectors(vector))
                .putAllPayload(Map.of(
                        "studyPostId", value(post.getId()),
                        "studyStatus", value(post.getStudyStatus() == null ? "" : post.getStudyStatus().name()),
                        "category", value(post.getCategory() == null ? "" : post.getCategory().name())))
                .build();
    }

    private <T> T await(ListenableFuture<T> future, String action) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VectorSearchException("Qdrant %s 중 인터럽트되었습니다.".formatted(action), e);
        } catch (ExecutionException e) {
            log.error("Qdrant {} 실패: collection={}, cause={}", action, props.collection(), e.getCause().toString());
            throw new VectorSearchException("Qdrant %s에 실패했습니다.".formatted(action), e.getCause());
        }
    }
}
