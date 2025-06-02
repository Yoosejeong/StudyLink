package com.studycrew.studyBoard.repository;

import com.studycrew.studyBoard.entity.StudyPosts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostsRepository extends JpaRepository<StudyPosts, Long> {
}
