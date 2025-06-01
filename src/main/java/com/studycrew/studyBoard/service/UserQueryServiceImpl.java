package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.UserHandler;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService{

    private final UserRepository userRepository;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));
    }

}
