package com.studycrew.studyBoard.entity.mapping;

import com.studycrew.studyBoard.entity.BaseEntity;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(name = "study_post_tags")
public class StudyPostTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="study_post_id", nullable = false)
    private StudyPost post;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="tag_id", nullable = false)
    private Tag tag;

}
