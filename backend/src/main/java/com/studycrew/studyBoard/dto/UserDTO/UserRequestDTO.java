package com.studycrew.studyBoard.dto.UserDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class UserRequestDTO {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSignUpRequestDTO {
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 8)
        private String password;
        @NotBlank
        private String username;
        @NotBlank
        @Size(max = 20)
        private String nickname;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class updateNicknameRequest {
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 20)
        private String nickname;
    }


}
