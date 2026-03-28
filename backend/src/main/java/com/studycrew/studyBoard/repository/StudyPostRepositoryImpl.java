package com.studycrew.studyBoard.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class StudyPostRepositoryImpl implements StudyPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public StudyPostRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public StudyPostCursorResponse searchByStatusAndNotDeleted(String rawKeyword, StudyStatus status,
                                                               LocalDateTime lastCreatedAt, Long lastId, int size) {
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
            where.and(Expressions.booleanTemplate(
                    "MATCH({0}) AGAINST ({1} IN BOOLEAN MODE)",
                    studyPost.title, rawKeyword.trim()
            ));
        }

        if (lastCreatedAt != null && lastId != null) {
            where.and(
                studyPost.createdAt.lt(lastCreatedAt)
                    .or(studyPost.createdAt.eq(lastCreatedAt).and(studyPost.id.lt(lastId)))
            );
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
                .limit(size + 1L)
                .fetch();

        boolean hasNext = content.size() > size;
        if (hasNext) {
            content.remove(size);
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
        LocalDateTime nextCursorCreatedAt = last != null ? last.getCreatedAt() : null;
        Long nextCursorId = last != null ? last.getStudyPostId() : null;

        return StudyPostCursorResponse.builder()
                .items(content)
                .hasNext(hasNext)
                .nextCursorCreatedAt(nextCursorCreatedAt)
                .nextCursorId(nextCursorId)
                .build();
    }

    private boolean hasText(String s) { return s != null && !s.isBlank(); }

}
