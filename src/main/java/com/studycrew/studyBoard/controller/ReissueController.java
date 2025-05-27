package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ReissueController {

    private final TokenService tokenService;

    @PostMapping("/api/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refresh = null;

        //request 에서 쿠키 받아오기
        Cookie[] cookies = request.getCookies();
        //refresh 토큰인 쿠키 refresh 변수에 저장하기
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
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | ExpiredJwtException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}



