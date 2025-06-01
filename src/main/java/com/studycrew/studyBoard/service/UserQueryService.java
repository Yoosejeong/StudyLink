package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.entity.User;

public interface UserQueryService {
    public User getUserByEmail(String email);
}
