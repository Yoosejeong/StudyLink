package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.jwt.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;

    public String reissueAccessToken(String refreshToken, HttpServletResponse response) {

        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token is null");
        }

        // 만료 시 예외 발생
        if (jwtUtil.isExpired(refreshToken)) {
            throw new IllegalArgumentException("Refresh token expired");
        }

        String category = jwtUtil.getCategory(refreshToken);

        if (!"refresh".equals(category)) {
            throw new IllegalArgumentException("Invalid token category");
        }

        String email = jwtUtil.getEmail(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // Redis에 저장된 현재 refresh와 동일한지 확인
        if (!refreshService.isValid(email, refreshToken)) {
            // 토큰 재사용/탈취 의심 → 강제 로그아웃 유도
            throw new IllegalArgumentException("일치하지 않는 토큰입니다.");
        }

        String newAccess = jwtUtil.createJwt("access", email, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", email, role, 86400000L);

        //redis에 refresh 저장
        refreshService.rotate(email, newRefresh, Duration.ofDays(1));

        response.addCookie(createCookie("refresh", newRefresh));

        return newAccess;
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
