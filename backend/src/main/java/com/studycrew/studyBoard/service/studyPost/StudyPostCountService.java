package com.studycrew.studyBoard.service.studyPost;

import com.studycrew.studyBoard.enums.StudyStatus;
import com.studycrew.studyBoard.repository.StudyPostRepository;
import com.studycrew.studyBoard.repository.StudyPostRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyPostCountService {

    private final StudyPostRepository studyPostRepository;

    @Cacheable(
            value = "studyPostCount",
            key = "T(com.studycrew.studyBoard.util.StudyPostCountKey).key(#status)",
            sync = true
    )
    @Transactional(readOnly = true)
    public long getCount(StudyStatus status) {
        return studyPostRepository.countByStatusAndNotDeleted(null, status);
    }

    @CacheEvict(value = "studyPostCount", allEntries = true)
    public void evictAll() { }
}

