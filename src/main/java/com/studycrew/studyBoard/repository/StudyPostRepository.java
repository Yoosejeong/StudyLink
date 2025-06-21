package com.studycrew.studyBoard.repository;

import com.studycrew.studyBoard.entity.StudyPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<StudyPost> findAll(Pageable pageable);
}
