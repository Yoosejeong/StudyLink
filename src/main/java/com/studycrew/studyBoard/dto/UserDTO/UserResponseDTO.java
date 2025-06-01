package com.studycrew.studyBoard.dto.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getUserDTO{
        private Long user_id;
        private String email;
        private String username;
        private String nickname;
    }

}
