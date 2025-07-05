package com.studycrew.studyBoard.repository;

import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {
    boolean existsByStudyPostAndUser(StudyPost studyPost, User user);
    List<StudyApplication> findByStudyPostId(Long studyPostId);
    List<StudyApplication> findByUserId(Long userId);
    List<StudyApplication> findAllByStudyPostAndApplicationStatus(StudyPost studyPost, ApplicationStatus applicationStatus);
}
