package com.studycrew.studyBoard.entity;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyPostHandler;
import com.studycrew.studyBoard.enums.StudyStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Table(name = "study_posts")
public class StudyPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    private String content;

    private int maxPeople;

    private int acceptedPeople;

    @Enumerated(EnumType.STRING)
    private StudyStatus studyStatus;

    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    public void update(String title, String content) {
        if (title != null) {
            System.out.println("title : " + title);
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }

    public void closeStudyPost() {
        if (this.studyStatus == StudyStatus.CLOSED) {
            throw new StudyPostHandler(ErrorStatus._STUDY_POST_ALREADY_CLOSED);
        }
        this.studyStatus = StudyStatus.CLOSED;
    }

    public void increaseAcceptedPeople() {
        this.acceptedPeople += 1;
    }

    public boolean isFullyBooked() {
        return this.acceptedPeople >= this.maxPeople;
    }

    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
