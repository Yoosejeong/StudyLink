package com.studycrew.studyBoard.service.user;

import com.studycrew.studyBoard.entity.User;

public interface UserQueryService {
    public User getUserByEmail(String email);
}
