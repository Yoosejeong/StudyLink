package com.studycrew.studyBoard.converter;


import com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO;
import com.studycrew.studyBoard.entity.User;

public class UserConverter {

    public static UserResponseDTO.getUserDTO togetUserDTO(User user){
        return UserResponseDTO.getUserDTO
                .builder()
                .user_id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .build();
    }

}
