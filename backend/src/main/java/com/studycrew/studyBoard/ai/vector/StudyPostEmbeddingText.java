package com.studycrew.studyBoard.ai.vector;

import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.enums.Category;

import java.util.Map;

public final class StudyPostEmbeddingText {

    // 한국어 질문("클라우드", "백엔드")과 의미가 가까워지도록 enum 이름 대신 한국어 라벨을 사용
    private static final Map<Category, String> CATEGORY_LABELS = Map.of(
            Category.BACKEND, "백엔드",
            Category.FRONTEND, "프론트엔드",
            Category.MOBILE, "모바일",
            Category.DATA_AI, "데이터/AI",
            Category.CLOUD_DEVOPS, "클라우드/DevOps",
            Category.ALGORITHM_CS, "알고리즘/CS",
            Category.GAME, "게임",
            Category.CERT, "자격증");

    private StudyPostEmbeddingText() {}

    public static String of(StudyPost post) {
        return "제목: %s\n카테고리: %s\n내용: %s".formatted(
                nullToEmpty(post.getTitle()), categoryLabel(post.getCategory()), nullToEmpty(post.getContent()));
    }

    static String categoryLabel(Category category) {
        return category == null ? "" : CATEGORY_LABELS.getOrDefault(category, category.name());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
