package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.entity.Refresh;
import com.studycrew.studyBoard.jwt.JWTUtil;
import com.studycrew.studyBoard.repository.RefreshRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public String reissueAccessToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token is null");
        }

        jwtUtil.isExpired(refreshToken); // 만료 시 예외 발생

        String category = jwtUtil.getCategory(refreshToken);
        if (!"refresh".equals(category)) {
            throw new IllegalArgumentException("Invalid token category");
        }

        boolean exists = refreshRepository.existsByRefreshToken(refreshToken);
        if (!exists) {
            throw new IllegalArgumentException("Refresh token not found in DB");
        }

        String email = jwtUtil.getEmail(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccess = jwtUtil.createJwt("access", email, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", email, role, 86400000L);

        refreshRepository.deleteByRefreshToken(refreshToken);
        addRefreshEntity(email, newRefresh, 86400000L);

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



    private void addRefreshEntity(String email, String refreshToken, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        Refresh refresh = Refresh.builder()
                .email(email)
                .refreshToken(refreshToken)
                .expiration(date.toString())
                .build();

        refreshRepository.save(refresh);
    }
}
