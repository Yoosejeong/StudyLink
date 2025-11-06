package com.studycrew.studyBoard.entity;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.StudyApplicationHandler;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import jakarta.persistence.*;
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
public class StudyApplication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_post_id")
    private StudyPost studyPost;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    @Version
    private Integer version;

    public void approve() {
        if (this.applicationStatus != ApplicationStatus.PENDING) {
            throw new StudyApplicationHandler(ErrorStatus._STUDY_APPLICATION_ALREADY_PROCESSED);
        }
        this.applicationStatus = ApplicationStatus.ACCEPTED;
    }

    public void reject() {
        if (this.applicationStatus != ApplicationStatus.PENDING) {
            throw new StudyApplicationHandler(ErrorStatus._STUDY_APPLICATION_ALREADY_PROCESSED);
        }
        this.applicationStatus = ApplicationStatus.REJECTED;
    }

    public void cancel() {
        if (this.applicationStatus != ApplicationStatus.PENDING) {
            throw new StudyApplicationHandler(ErrorStatus._STUDY_APPLICATION_ALREADY_PROCESSED);
        }
        this.applicationStatus = ApplicationStatus.CANCELED;
    }

}
