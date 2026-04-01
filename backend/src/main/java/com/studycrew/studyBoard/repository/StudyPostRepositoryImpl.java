package com.studycrew.studyBoard.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studycrew.studyBoard.dto.StudyPostDTO.QStudyPostResponseDTO_GetStudyPostListResponse;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.StudyPostCursorResponse;
import com.studycrew.studyBoard.entity.QStudyPost;
import com.studycrew.studyBoard.entity.QTag;
import com.studycrew.studyBoard.entity.QUser;
import com.studycrew.studyBoard.entity.mapping.QStudyPostTag;
import com.studycrew.studyBoard.enums.StudyStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class StudyPostRepositoryImpl implements StudyPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    public StudyPostRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
        this.em = em;
    }

    @Override
    public StudyPostCursorResponse searchByStatusAndNotDeleted(String rawKeyword, StudyStatus status,
                                                               LocalDateTime lastCreatedAt, Long lastId, int size) {
        QStudyPost studyPost = QStudyPost.studyPost;
        QUser user = QUser.user;
        QStudyPostTag spt = QStudyPostTag.studyPostTag;
        QTag tag = QTag.tag;

        boolean hasNext = false;
        List<Long> fulltextIds = null;

        if (hasText(rawKeyword)) {
            List<Long> candidateIds = getFulltextMatchingIds(sanitizeFulltextKeyword(rawKeyword), status, lastCreatedAt, lastId, size + 1);
            hasNext = candidateIds.size() > size;
            fulltextIds = hasNext ? new ArrayList<>(candidateIds.subList(0, size)) : candidateIds;

            if (fulltextIds.isEmpty()) {
                return StudyPostCursorResponse.builder()
                        .items(Collections.emptyList())
                        .hasNext(false)
                        .nextCursorCreatedAt(null)
                        .nextCursorId(null)
                        .build();
            }
        }

        BooleanBuilder where = new BooleanBuilder()
                .and(studyPost.deleted.isFalse());

        if (fulltextIds != null) {
            // FULLTEXT로 추린 ID만 조회 (status·cursor는 native query에서 이미 적용)
            where.and(studyPost.id.in(fulltextIds));
        } else {
            if (status != null) {
                where.and(studyPost.studyStatus.eq(status));
            }
            if (lastCreatedAt != null && lastId != null) {
                where.and(
                    studyPost.createdAt.lt(lastCreatedAt)
                        .or(studyPost.createdAt.eq(lastCreatedAt).and(studyPost.id.lt(lastId)))
                );
            }
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
                .limit(fulltextIds != null ? (long) size : size + 1L)
                .fetch();

        if (fulltextIds == null) {
            hasNext = content.size() > size;
            if (hasNext) content.remove(size);
        }

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

        GetStudyPostListResponse last = hasNext ? content.get(content.size() - 1) : null;

        return StudyPostCursorResponse.builder()
                .items(content)
                .hasNext(hasNext)
                .nextCursorCreatedAt(last != null ? last.getCreatedAt() : null)
                .nextCursorId(last != null ? last.getStudyPostId() : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<Long> getFulltextMatchingIds(String keyword, StudyStatus status,
                                               LocalDateTime lastCreatedAt, Long lastId, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id FROM study_posts
                WHERE deleted = false
                  AND MATCH(title) AGAINST (:keyword IN BOOLEAN MODE)
                """);

        if (status != null) {
            sql.append(" AND study_status = :status");
        }
        if (lastCreatedAt != null && lastId != null) {
            sql.append(" AND (created_at < :lastCreatedAt OR (created_at = :lastCreatedAt AND id < :lastId))");
        }
        sql.append(" ORDER BY created_at DESC, id DESC LIMIT :limit");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("keyword", keyword);
        query.setParameter("limit", limit);

        if (status != null) {
            query.setParameter("status", status.name());
        }
        if (lastCreatedAt != null && lastId != null) {
            query.setParameter("lastCreatedAt", lastCreatedAt);
            query.setParameter("lastId", lastId);
        }

        return ((List<?>) query.getResultList()).stream()
                .map(id -> ((Number) id).longValue())
                .collect(Collectors.toList());
    }

    private String sanitizeFulltextKeyword(String keyword) {
        return keyword.replaceAll("[+\\-><()~*\"@]+", " ").trim();
    }

    private boolean hasText(String s) { return s != null && !s.isBlank(); }
}
