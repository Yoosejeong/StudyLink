package com.studycrew.studyBoard.service.studyApplication;

import com.studycrew.studyBoard.entity.StudyApplication;
import com.studycrew.studyBoard.entity.User;

public interface StudyApplicationCommandService {
    StudyApplication applyStudyApplication(Long studyPostId, User user);
    StudyApplication approveStudyApplication(Long studyApplicationId, User user);
}
