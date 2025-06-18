package com.studycrew.studyBoard.service.user;

import com.studycrew.studyBoard.dto.UserDTO.UserSignUpRequestDTO;

public interface UserCommandService {
    void joinProcess(UserSignUpRequestDTO joinRequestDTO);
}
