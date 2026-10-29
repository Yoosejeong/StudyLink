package com.studycrew.studyBoard.evaluation;

import com.studycrew.studyBoard.ai.StudySearchTool;
import com.studycrew.studyBoard.ai.StudySearchTool.StudySummary;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.StudyPostCursorResponse;
import com.studycrew.studyBoard.enums.SearchTarget;
import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.service.studyPost.StudyPostQueryService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * InnoDB FULLTEXT 인덱스는 커밋된 데이터만 검색하므로 @Transactional 롤백을 쓸 수 없어
 * 별도 스키마(studycrewdb_search_test)에 픽스처를 커밋한다.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://localhost:3306/studycrewdb_search_test?createDatabaseIfNotExist=true"
                + "&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudyPostSearchIntegrationTest {

    private static final long SPRING_IN_CONTENT = 1;
    private static final long REACT_IN_TITLE = 2;
    private static final long REACT_IN_CONTENT = 3;
    private static final long REACT_CLOSED = 4;
    private static final long REACT_DELETED = 5;
    private static final long REACT_NATIVE_CONTENT = 6;
    private static final long UNRELATED = 7;

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired StudyPostQueryService studyPostQueryService;
    @Autowired StudySearchTool studySearchTool;

    @BeforeAll
    void setUp() {
        jdbcTemplate.execute("ALTER TABLE study_posts ADD FULLTEXT INDEX ft_study_posts_title (title) WITH PARSER ngram");
        jdbcTemplate.execute("ALTER TABLE study_posts ADD FULLTEXT INDEX ft_study_posts_title_content (title, content) WITH PARSER ngram");

        jdbcTemplate.update("""
                INSERT INTO users (id, email, password, username, nickname, role, profile_key, created_at, updated_at)
                VALUES (1, 'tester@test.com', 'pw', 'tester', '테스터', 'ROLE_USER', 'profile/1.png', NOW(), NOW())
                """);
        insertPost(SPRING_IN_CONTENT, "백엔드 스터디 모집", "Spring Boot 핵심 개념을 함께 학습합니다.", StudyStatus.RECRUITING, false);
        insertPost(REACT_IN_TITLE, "React 입문 스터디", "프론트엔드 기초를 함께 공부합니다.", StudyStatus.RECRUITING, false);
        insertPost(REACT_IN_CONTENT, "프론트엔드 협업 스터디", "React 기반 협업 프로젝트를 진행합니다.", StudyStatus.RECRUITING, false);
        insertPost(REACT_CLOSED, "React 심화 스터디", "마감된 모집글입니다.", StudyStatus.CLOSED, false);
        insertPost(REACT_DELETED, "React 삭제 스터디", "삭제된 모집글입니다.", StudyStatus.RECRUITING, true);
        insertPost(REACT_NATIVE_CONTENT, "모바일 앱 기초", "React Native로 간단한 앱을 만듭니다.", StudyStatus.RECRUITING, false);
        insertPost(UNRELATED, "알고리즘 문제풀이", "백준 문제를 함께 풉니다.", StudyStatus.RECRUITING, false);

        jdbcTemplate.update("INSERT INTO tag (id, name) VALUES (1, 'React')");
        jdbcTemplate.update("""
                INSERT INTO study_post_tags (study_post_id, tag_id, created_at, updated_at)
                VALUES (?, 1, NOW(), NOW())
                """, REACT_IN_CONTENT);
    }

    private void insertPost(long id, String title, String content, StudyStatus studyStatus, boolean deleted) {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0).plusMinutes(id);
        jdbcTemplate.update("""
                INSERT INTO study_posts (id, user_id, title, content, max_people, accepted_people,
                                         study_status, deleted, category, created_at, updated_at)
                VALUES (?, 1, ?, ?, 5, 0, ?, ?, 'FRONTEND', ?, ?)
                """, id, title, content, studyStatus.name(), deleted, createdAt, createdAt);
    }

    @Test
    void 일반_검색_API는_제목과_본문을_검색하고_기존_DTO를_반환한다() throws Exception {
        mockMvc.perform(get("/api/study-posts")
                        .param("rawKeyword", "React")
                        .param("status", "RECRUITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items[*].studyPostId").value(
                        contains((int) REACT_NATIVE_CONTENT, (int) REACT_IN_CONTENT, (int) REACT_IN_TITLE)))
                .andExpect(jsonPath("$.result.items[1].title").value("프론트엔드 협업 스터디"))
                .andExpect(jsonPath("$.result.items[1].nickname").value("테스터"))
                .andExpect(jsonPath("$.result.items[1].tags[0]").value("React"))
                .andExpect(jsonPath("$.result.items[1].profileUrl").exists())
                .andExpect(jsonPath("$.result.items[0].searchScore").doesNotExist())
                .andExpect(jsonPath("$.result.hasNext").value(false));
    }

    @Test
    void 본문에만_있는_키워드는_제목_검색에서는_안나오고_제목본문_검색에서만_나온다() {
        assertThat(searchIds(SearchTarget.TITLE, "Spring Boot", 10)).isEmpty();
        assertThat(searchIds(SearchTarget.TITLE_CONTENT, "Spring Boot", 10)).containsExactly(SPRING_IN_CONTENT);

        assertThat(searchIds(SearchTarget.TITLE, "React", 10)).containsExactly(REACT_IN_TITLE);
        assertThat(searchIds(SearchTarget.TITLE_CONTENT, "React", 10))
                .containsExactly(REACT_NATIVE_CONTENT, REACT_IN_CONTENT, REACT_IN_TITLE);
    }

    @Test
    void AI_Tool은_일반_검색과_같은_Service_결과를_사용한다() {
        StudySearchTool.SearchResult toolResult = studySearchTool.searchStudies("React");

        assertThat(toolResult.status()).isEqualTo("OK");
        assertThat(toolResult.studies()).extracting(StudySummary::studyPostId)
                .containsExactlyElementsOf(searchIds(SearchTarget.TITLE_CONTENT, "React", 5));
        assertThat(toolResult.studies()).extracting(StudySummary::studyPostId)
                .containsExactly(REACT_NATIVE_CONTENT, REACT_IN_CONTENT, REACT_IN_TITLE);
    }

    @Test
    void 모집_마감글과_삭제글은_제외된다() throws Exception {
        // 상태 필터가 없으면 마감글은 포함, 삭제글은 항상 제외
        mockMvc.perform(get("/api/study-posts").param("rawKeyword", "React"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items[*].studyPostId").value(contains(
                        (int) REACT_NATIVE_CONTENT, (int) REACT_CLOSED, (int) REACT_IN_CONTENT, (int) REACT_IN_TITLE)));

        assertThat(searchIds(SearchTarget.TITLE_CONTENT, "React", 10))
                .doesNotContain(REACT_CLOSED, REACT_DELETED);
    }

    @Test
    void 검색어가_없으면_기존_목록_조회와_동일하다() throws Exception {
        mockMvc.perform(get("/api/study-posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items[*].studyPostId").value(contains(
                        (int) UNRELATED, (int) REACT_NATIVE_CONTENT, (int) REACT_CLOSED, (int) REACT_IN_CONTENT,
                        (int) REACT_IN_TITLE, (int) SPRING_IN_CONTENT)));

        List<Long> legacy = ids(studyPostQueryService.getStudyPostList(null, null, null, null, 12));
        List<Long> titleContent = ids(studyPostQueryService.getStudyPostList(
                SearchTarget.TITLE_CONTENT, null, null, null, null, 12));
        assertThat(titleContent).isEqualTo(legacy);
    }

    @Test
    void 커서_페이징이_정상_동작한다() throws Exception {
        StudyPostCursorResponse first = studyPostQueryService.getStudyPostList(
                SearchTarget.TITLE_CONTENT, "React", StudyStatus.RECRUITING, null, null, 2);
        assertThat(ids(first)).containsExactly(REACT_NATIVE_CONTENT, REACT_IN_CONTENT);
        assertThat(first.isHasNext()).isTrue();
        assertThat(first.getNextCursorId()).isEqualTo(REACT_IN_CONTENT);

        mockMvc.perform(get("/api/study-posts")
                        .param("rawKeyword", "React")
                        .param("status", "RECRUITING")
                        .param("size", "2")
                        .param("lastCreatedAt", first.getNextCursorCreatedAt().toString())
                        .param("lastId", String.valueOf(first.getNextCursorId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items[*].studyPostId").value(contains((int) REACT_IN_TITLE)))
                .andExpect(jsonPath("$.result.hasNext").value(false));
    }

    @Test
    void 검색어_길이_검증과_잘못된_입력_처리가_유지된다() throws Exception {
        mockMvc.perform(get("/api/study-posts").param("rawKeyword", "R"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/study-posts").param("rawKeyword", "가".repeat(51)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/study-posts").param("rawKeyword", "React").param("size", "51"))
                .andExpect(status().isBadRequest());

        assertThat(studySearchTool.searchStudies("R").status()).isEqualTo("INVALID_KEYWORD");
        assertThat(studySearchTool.searchStudies("+++").status()).isEqualTo("INVALID_KEYWORD");
    }

    private List<Long> searchIds(SearchTarget target, String keyword, int size) {
        return ids(studyPostQueryService.getStudyPostList(target, keyword, StudyStatus.RECRUITING, null, null, size));
    }

    private List<Long> ids(StudyPostCursorResponse response) {
        return response.getItems().stream().map(GetStudyPostListResponse::getStudyPostId).toList();
    }
}
