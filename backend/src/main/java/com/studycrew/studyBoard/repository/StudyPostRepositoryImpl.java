package com.studycrew.studyBoard.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studycrew.studyBoard.dto.StudyPostDTO.QStudyPostResponseDTO_GetStudyPostListResponse;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.QStudyPost;
import com.studycrew.studyBoard.entity.QTag;
import com.studycrew.studyBoard.entity.QUser;
import com.studycrew.studyBoard.entity.mapping.QStudyPostTag;
import com.studycrew.studyBoard.enums.StudyStatus;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        QStudyPostTag spt = QStudyPostTag.studyPostTag;
        QTag tag = QTag.tag;


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
                        studyPost.studyStatus, studyPost.category, user.profileKey, studyPost.createdAt, studyPost.updatedAt
                ))
                .from(studyPost)
                .leftJoin(studyPost.user, user)
                .where(where)
                .orderBy(studyPost.createdAt.desc(), studyPost.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(studyPost.id.count())
                .from(studyPost)
                .where(where)
                .fetchOne();

        if (!content.isEmpty()) {
            List<Long> ids = content.stream()
                    .map(GetStudyPostListResponse::getStudyPostId)
                    .toList();

            List<Tuple> rows = queryFactory
                    .select(spt.post.id, tag.name)
                    .from(spt)
                    .join(spt.tag, tag)
                    .where(spt.post.id.in(ids))
                    .fetch();

            Map<Long, List<String>> tagMap = rows.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.get(spt.post.id),
                            LinkedHashMap::new,
                            Collectors.mapping(t -> t.get(tag.name), Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> list.stream()
                                            .filter(Objects::nonNull)
                                            .distinct()
                                            .collect(Collectors.toList())
                            ))
                    ));

            content.forEach(dto ->
                    dto.attachTags(tagMap.getOrDefault(dto.getStudyPostId(), Collections.emptyList())));
        }
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
