package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "JWT 토큰 재발급 관련 API")
@RequiredArgsConstructor
@RestController
public class ReissueController {

    private final TokenService tokenService;

    @Operation(
            summary = "Access/Refresh 토큰 재발급",
            description = """
        클라이언트의 쿠키에 저장된 Refresh Token을 기반으로
        Access Token과 Refresh Token을 모두 새로 발급합니다.
        
        - Access Token은 Authorization 헤더에 포함됩니다.
        - Refresh Token은 HttpOnly 쿠키로 재설정됩니다.
        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Access 토큰이 재발급되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                {
                  "isSuccess": true,
                  "code": "AUTH2000",
                  "message": "Access 토큰이 재발급되었습니다."
                }
                """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 Refresh 토큰입니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                {
                  "isSuccess": false,
                  "code": "AUTH4010",
                  "message": "유효하지 않은 Refresh 토큰입니다."
                }
                """
                            )
                    )
            )
    })

    @PostMapping("/api/reissue")
    public ResponseEntity<ApiResponse<Void>> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = null;

        // request 에서 쿠키 받아오기
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                }
            }
        }

        try {
            String newAccess = tokenService.reissueAccessToken(refresh, response);
            response.setHeader("Authorization", "Bearer " + newAccess);

            return ResponseEntity
                    .status(SuccessStatus._USER_REISSUED.getHttpStatus()) // ex) 200 OK
                    .body(ApiResponse.of(SuccessStatus._USER_REISSUED));

        } catch (IllegalArgumentException | ExpiredJwtException e) {
            return ResponseEntity
                    .status(ErrorStatus._INVALID_REFRESH_TOKEN.getHttpStatus()) // ex) 401 Unauthorized
                    .body(ApiResponse.onFailure(ErrorStatus._INVALID_REFRESH_TOKEN));
        }
    }
}




