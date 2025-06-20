package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.converter.UserConverter;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO.getUserDTO;
import com.studycrew.studyBoard.dto.UserDTO.UserSignUpRequestDTO;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.service.user.UserCommandServiceImpl;
import com.studycrew.studyBoard.service.user.UserQueryServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserCommandServiceImpl userCommandService;
    private final UserQueryServiceImpl userQueryService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임, 이름 정보를 받아 회원가입을 진행합니다.")
    @PostMapping("/api/signUp")
    public ResponseEntity<ApiResponse<Void>> SignUp(@RequestBody @Valid UserSignUpRequestDTO userSignUpRequestDTO) {

        userCommandService.joinProcess(userSignUpRequestDTO);
        return ResponseEntity
                .status(SuccessStatus._USER_CREATED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus._USER_CREATED));
    }

    @Operation(summary = "회원 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다.")
    @GetMapping("/api/users")
    public ApiResponse<getUserDTO> getUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        String email = userDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        getUserDTO getUserDTO = UserConverter.togetUserDTO(user);
        return ApiResponse.onSuccess(getUserDTO);
    }

}
