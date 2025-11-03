package com.studycrew.studyBoard.repository;

import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.StudyPost;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.enums.ApplicationStatus;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;

@Repository
public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {
    boolean existsByStudyPostAndUserAndApplicationStatusIn(StudyPost studyPost, User user, List<ApplicationStatus> applicationStatus);
    List<StudyApplication> findByStudyPostId(Long studyPostId);
    List<StudyApplication> findByUserId(Long userId);
    List<StudyApplication> findAllByStudyPostAndApplicationStatus(StudyPost studyPost, ApplicationStatus applicationStatus);
    Optional<StudyApplication> findTopByUserIdAndStudyPostIdOrderByIdDesc(Long userId, Long studyPostId);
}
