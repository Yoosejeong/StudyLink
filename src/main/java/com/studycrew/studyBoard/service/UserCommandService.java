package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.dto.UserDTO.UserSignUpRequestDTO;

public interface UserCommandService {
    boolean joinProcess(UserSignUpRequestDTO joinRequestDTO);
}
