package com.studycrew.studyBoard.service.user;

import com.studycrew.studyBoard.entity.User;

import static com.studycrew.studyBoard.dto.UserDTO.UserRequestDTO.*;


public interface UserCommandService {
    void joinProcess(UserSignUpRequestDTO joinRequestDTO);
    User updateNickname(User user, updateNicknameRequest requestDTO);
}
