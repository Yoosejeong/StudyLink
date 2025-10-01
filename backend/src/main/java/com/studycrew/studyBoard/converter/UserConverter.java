package com.studycrew.studyBoard.converter;


import com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO;
import com.studycrew.studyBoard.entity.User;

public class UserConverter {

    public static UserResponseDTO.getUserDTO toGetUserDTO(User user) {
        return UserResponseDTO.getUserDTO
                .builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .build();
    }

    public static UserResponseDTO.updateNicknameDTO toUpdateNicknameDTO(User user) {
        return UserResponseDTO.updateNicknameDTO
                .builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }

    public static UserResponseDTO.checkNicknameDTO checkNicknameDTO(boolean available) {
        return UserResponseDTO.checkNicknameDTO
                .builder()
                .available(available)
                .build();
    }

}
