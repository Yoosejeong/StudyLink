package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.service.S3Service;
import com.studycrew.studyBoard.service.user.UserQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.studycrew.studyBoard.dto.S3DTO.S3RequestDTO.*;
import static com.studycrew.studyBoard.dto.S3DTO.S3ResponseDTO.*;
import static com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {
    private final S3Service s3Service;
    private final UserQueryService userQueryService;

    @PostMapping("/presign/upload")
    public ApiResponse<PresignPutResponse> presignPut(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody @Valid PresignPutRequest req) {
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        PresignPutResponse responseDTO = s3Service.presignPut(user.getId(), req);
        return ApiResponse.of(SuccessStatus._PROFILE_UPLOAD_SUCCESS, responseDTO);
    }

    @GetMapping("/presign/me")
    public ApiResponse<headerProfileDTO> getHeaderProfile(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        headerProfileDTO responseDTO = s3Service.getMeHeader(user.getId());
        return ApiResponse.of(SuccessStatus._PROFILE_RETRIEVED, responseDTO);
    }

    @PostMapping("/presign/confirm")
    public ApiResponse<Void> confirmProfile(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                            @RequestBody @Valid ConfirmProfileRequest req) {
        User user = userQueryService.getUserByEmail(customUserDetails.getUsername());
        s3Service.confirmProfile(user.getId(), req.getNewKey());
        return ApiResponse.of(SuccessStatus._PROFILE_UPDATED);
    }

}
