package com.studycrew.studyBoard.evaluation.core;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class RetrievalMetricsTest {

    private static final Map<Long, Integer> REL = Map.of(1L, 2, 2L, 1);

    @Test
    void 정답을_모두_찾으면_recall은_1이다() {
        assertThat(RetrievalMetrics.recallAtK(List.of(9L, 1L, 2L), REL, 5)).isEqualTo(1.0);
    }

    @Test
    void recall은_정답_수_대비_상위_k개_안의_정답_비율이다() {
        Map<Long, Integer> sixRelevant = Map.of(1L, 2, 2L, 2, 3L, 1, 4L, 1, 5L, 1, 6L, 1);
        assertThat(RetrievalMetrics.recallAtK(List.of(1L, 2L, 3L, 4L, 5L, 6L), sixRelevant, 5))
                .isCloseTo(5.0 / 6, within(1e-9));
    }

    @Test
    void k_밖의_정답은_세지_않는다() {
        assertThat(RetrievalMetrics.recallAtK(List.of(7L, 8L, 9L, 10L, 11L, 1L), REL, 5)).isZero();
        assertThat(RetrievalMetrics.ndcgAtK(List.of(7L, 8L, 9L, 10L, 11L, 1L), REL, 5)).isZero();
    }

    @Test
    void 중복_ID는_한_번만_센다() {
        assertThat(RetrievalMetrics.recallAtK(List.of(1L, 1L), Map.of(1L, 2, 2L, 2), 5)).isEqualTo(0.5);
    }

    @Test
    void 관련도_0은_정답이_아니다() {
        assertThat(RetrievalMetrics.recallAtK(List.of(1L), Map.of(1L, 2, 2L, 0), 5)).isEqualTo(1.0);
    }

    @Test
    void 이상적인_순서면_nDCG는_1이다() {
        assertThat(RetrievalMetrics.ndcgAtK(List.of(1L, 2L), REL, 5)).isCloseTo(1.0, within(1e-9));
    }

    @Test
    void 관련도가_낮은_글이_먼저_오면_nDCG가_낮아진다() {
        // DCG  = (2^1-1)/log2(2) + (2^2-1)/log2(3) = 1 + 1.892789 = 2.892789
        // IDCG = (2^2-1)/log2(2) + (2^1-1)/log2(3) = 3 + 0.630930 = 3.630930
        assertThat(RetrievalMetrics.ndcgAtK(List.of(2L, 1L), REL, 5)).isCloseTo(0.796707, within(1e-6));
    }

    @Test
    void 정답이_k보다_많으면_이상적_DCG도_k개까지만_계산한다() {
        Map<Long, Integer> threeRelevant = Map.of(1L, 2, 2L, 2, 3L, 2);
        assertThat(RetrievalMetrics.ndcgAtK(List.of(1L, 2L), threeRelevant, 2)).isCloseTo(1.0, within(1e-9));
    }

    @Test
    void 결과가_없으면_0이다() {
        assertThat(RetrievalMetrics.recallAtK(List.of(), REL, 5)).isZero();
        assertThat(RetrievalMetrics.ndcgAtK(List.of(), REL, 5)).isZero();
    }

    @Test
    void 정답이_없는_질문은_계산할_수_없다() {
        assertThatThrownBy(() -> RetrievalMetrics.recallAtK(List.of(1L), Map.of(), 5))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RetrievalMetrics.ndcgAtK(List.of(1L), Map.of(1L, 0), 5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
