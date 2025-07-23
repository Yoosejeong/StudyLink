package com.studycrew.studyBoard.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.studycrew.studyBoard.dto.StudyPostDTO.QStudyPostResponseDTO_GetStudyPostListResponse;
import com.studycrew.studyBoard.dto.StudyPostDTO.StudyPostResponseDTO.GetStudyPostListResponse;
import com.studycrew.studyBoard.entity.QStudyPost;
import static com.studycrew.studyBoard.entity.QStudyPost.*;
import com.studycrew.studyBoard.entity.QUser;
import static com.studycrew.studyBoard.entity.QUser.*;
import com.studycrew.studyBoard.entity.StudyPost;
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
    public Page<GetStudyPostListResponse> searchByStatusAndNotDeleted(StudyStatus status, Pageable pageable) {
        QStudyPost studyPost = QStudyPost.studyPost;
        QUser user = QUser.user;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(studyPost.deleted.eq(false));
        if (status != null) {
            builder.and(studyPost.studyStatus.eq(status));
        }

        List<GetStudyPostListResponse> content = queryFactory
                .select(new QStudyPostResponseDTO_GetStudyPostListResponse(
                        studyPost.id,
                        studyPost.title,
                        user.nickname,
                        studyPost.maxPeople,
                        studyPost.acceptedPeople,
                        studyPost.studyStatus,
                        studyPost.createdAt,
                        studyPost.updatedAt
                ))
                .from(studyPost)
                .leftJoin(studyPost.user, user)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(studyPost.createdAt.desc())
                .fetch();

        Long count = queryFactory
                .select(studyPost.count())
                .from(studyPost)
                .where(builder)
                .fetchOne();
        long total = count != null ? count : 0L;

        return new PageImpl<>(content, pageable, total);

    }
}
