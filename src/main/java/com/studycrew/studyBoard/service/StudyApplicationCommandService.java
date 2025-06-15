package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.User;

public interface StudyApplicationCommandService {
    StudyApplication applyStudyApplication(Long studyPostId, User user);
}
