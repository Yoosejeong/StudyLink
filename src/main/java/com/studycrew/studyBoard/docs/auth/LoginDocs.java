package com.studycrew.studyBoard.docs.auth;

import com.studycrew.studyBoard.dto.UserDTO.UserLoginRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "로그인", description = "로그인 관련 API")
public interface LoginDocs {
    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 - AccessToken은 Authorization 헤더에, RefreshToken은 쿠키로 반환됩니다.",
                    content = @Content
            ),
            @ApiResponse(responseCode = "401", description = "로그인 실패"),
    })
    @PostMapping("/api/login")
    default void login(@RequestBody @Parameter(description = "로그인 요청 DTO") UserLoginRequestDTO request) {
        throw new UnsupportedOperationException("Spring Security 필터에서 처리됩니다.");
    }
}
