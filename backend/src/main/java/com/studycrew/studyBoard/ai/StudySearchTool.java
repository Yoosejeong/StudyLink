package com.studycrew.studyBoard.ai;

import com.studycrew.studyBoard.enums.Category;
import com.studycrew.studyBoard.enums.SearchTarget;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.service.studyPost.StudyPostQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudySearchTool {
    private final StudyPostQueryService studyPostQueryService;

    @Tool(description = "StudyLink DB에서 제목과 본문 키워드로 모집 중인 스터디를 최신순 최대 5개 검색합니다. 자연어 문장 대신 핵심 기술 또는 주제 키워드를 사용하세요.")
    public SearchResult searchStudies(
            @ToolParam(description = "공백 제거 후 2~50자의 제목/본문 검색어. 예: Java, Spring Boot, 알고리즘") String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.length() < 2 || normalized.length() > 50
                || normalized.replaceAll("[+\\-><()~*\"@]+", " ").isBlank()) {
            log.info("AI searchStudies rejected: invalid keyword");
            return new SearchResult("INVALID_KEYWORD", "검색어는 공백이나 검색 연산자만으로 구성할 수 없으며 2~50자여야 합니다.", List.of());
        }
        log.info("AI searchStudies invoked");
        try {
            var response = studyPostQueryService.getStudyPostList(
                    SearchTarget.TITLE_CONTENT, normalized, StudyStatus.RECRUITING, null, null, 5);
            var items = response.getItems().stream()
                    .map(item -> new StudySummary(item.getStudyPostId(), item.getTitle(),
                            item.getCategory(), item.getTags(), item.getStudyStatus()))
                    .toList();
            log.info("AI searchStudies completed: count={}, ids={}", items.size(),
                    items.stream().map(StudySummary::studyPostId).toList());
            return new SearchResult("OK", items.isEmpty() ? "검색 결과가 없습니다." : "DB 검색 완료", items);
        } catch (RuntimeException exception) {
            log.error("AI searchStudies failed", exception);
            throw exception;
        }
    }

    public record SearchResult(String status, String message, List<StudySummary> studies) {}
    public record StudySummary(Long studyPostId, String title, Category category,
                               List<String> tags, StudyStatus studyStatus) {}
}
