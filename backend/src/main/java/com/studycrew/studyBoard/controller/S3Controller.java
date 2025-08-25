package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.dto.CustomUserDetails;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.service.S3Service;
import com.studycrew.studyBoard.service.user.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.studycrew.studyBoard.dto.S3DTO.S3RequestDTO.*;
import static com.studycrew.studyBoard.dto.S3DTO.S3ResponseDTO.*;
import static com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
public class S3Controller {
    private final S3Service s3Service;
    private final UserQueryService userQueryService;

    @PostMapping("/presign/put")
    public ApiResponse<PresignPutResponse> presignPut(@AuthenticationPrincipal CustomUserDetails customUserDetails, @RequestBody @Valid PresignPutRequest req) {
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        PresignPutResponse responseDTO = s3Service.presignPut(user.getId(), req);
        return ApiResponse.of(SuccessStatus._PROFILE_UPLOAD_SUCCESS, responseDTO);
    }

    @GetMapping("/presign/profile")
    public ApiResponse<headerProfileDTO> getHeaderProfile(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        headerProfileDTO responseDTO = s3Service.getMeHeader(user.getId());
        return ApiResponse.of(SuccessStatus._PROFILE_RETRIEVED, responseDTO);
    }
}
