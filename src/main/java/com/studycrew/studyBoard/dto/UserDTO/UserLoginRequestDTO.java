package com.studycrew.studyBoard.dto.UserDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class UserLoginRequestDTO {

    private String email;
    private String password;

}
