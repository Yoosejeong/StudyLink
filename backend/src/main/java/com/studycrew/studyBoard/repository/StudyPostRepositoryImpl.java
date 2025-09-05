package com.studycrew.studyBoard.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studycrew.studyBoard.dto.StudyPostDTO.QStudyPostResponseDTO_GetStudyPostListResponse;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.QStudyPost;
import com.studycrew.studyBoard.entity.QUser;
import com.studycrew.studyBoard.enums.StudyStatus;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


public class StudyPostRepositoryImpl implements StudyPostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public StudyPostRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<GetStudyPostListResponse> searchByStatusAndNotDeleted(String rawKeyword, StudyStatus status, Pageable pageable) {
        QStudyPost studyPost = QStudyPost.studyPost;
        QUser user = QUser.user;

        BooleanBuilder where = new BooleanBuilder()
                .and(studyPost.deleted.isFalse());

        if (status != null) {
            where.and(studyPost.studyStatus.eq(status));
        }

        if (hasText(rawKeyword)) {
            String key  = normalize(rawKeyword);       // 공백 제거 + 소문자
            String safe = escapeWildcards(key);        // %,_ 리터럴 처리
            where.and(Expressions.booleanTemplate(
                    "REPLACE(LOWER({0}), ' ', '') LIKE CONCAT('%', {1}, '%') ESCAPE '\\'",
                    studyPost.title, safe
            ));
        }

        List<GetStudyPostListResponse> content = queryFactory
                .select(new QStudyPostResponseDTO_GetStudyPostListResponse(
                        studyPost.id, studyPost.title, user.nickname,
                        studyPost.maxPeople, studyPost.acceptedPeople,
                        studyPost.studyStatus, studyPost.createdAt, studyPost.updatedAt
                ))
                .from(studyPost)
                .leftJoin(studyPost.user, user)
                .where(where)
                .orderBy(studyPost.createdAt.desc(), studyPost.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // total (불필요한 조인 제거)
        Long total = queryFactory
                .select(studyPost.id.count())
                .from(studyPost)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private boolean hasText(String s) { return s != null && !s.isBlank(); }

    private String normalize(String raw) {
        return raw.trim().replaceAll("\\s+", "").toLowerCase(java.util.Locale.ROOT);
    }

    private String escapeWildcards(String s) {
        return s.replace("\\","\\\\").replace("%","\\%").replace("_","\\_");
    }
}
