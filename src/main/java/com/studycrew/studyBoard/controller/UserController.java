package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.UserConverter;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO;
import com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO.getUserDTO;
import com.studycrew.studyBoard.dto.UserDTO.UserSignUpRequestDTO;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.service.UserCommandServiceImpl;
import com.studycrew.studyBoard.service.UserQueryService;
import com.studycrew.studyBoard.service.UserQueryServiceImpl;
import com.sun.net.httpserver.Authenticator.Success;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserCommandServiceImpl userCommandService;
    private final UserQueryServiceImpl userQueryService;

    @PostMapping("/api/signUp")
    public ResponseEntity<ApiResponse<Void>> SignUp(@RequestBody @Valid UserSignUpRequestDTO userSignUpRequestDTO) {

        userCommandService.joinProcess(userSignUpRequestDTO);
        return ResponseEntity
                .status(SuccessStatus._USER_CREATED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus._USER_CREATED));
    }

    @GetMapping("/api/users")
    public ApiResponse<getUserDTO> getUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        String email = userDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        getUserDTO getUserDTO = UserConverter.togetUserDTO(user);
        return ApiResponse.onSuccess(getUserDTO);
    }

}
