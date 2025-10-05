package com.studycrew.studyBoard.jwt;

import com.studycrew.studyBoard.service.RefreshService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        //path and method verify
        String requestUri = request.getRequestURI();

        //로그아웃 경로가 아니면 다음 필터로 넘어감
        if (!requestUri.matches("^\\/api\\/logout$")) {

            filterChain.doFilter(request, response);
            return;
        }

        //POST 요청이 아니면 다음 필터로 넘어감
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {

            filterChain.doFilter(request, response);
            return;
        }

        //쿠키를 모두 불러와서 리프래시 토큰이 있는지 확인 후 refresh 변수에 담음
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }
        }

        // 항상 삭제 쿠키 내려주기 (멱등)
        Cookie removal = new Cookie("refresh", "");
        removal.setMaxAge(0);
        removal.setPath("/");
        removal.setHttpOnly(true);
        response.addCookie(removal);

        //서버 상태(예: Redis 키) 정리: 실패해도 무시
        if (refresh != null && !refresh.isBlank()) {
            try {
                String category = jwtUtil.getCategory(refresh);
                if ("refresh".equals(category)) {
                    String email = jwtUtil.getEmail(refresh);
                    if (email != null) {
                        refreshService.revoke(email);
                    }
                }
            } catch (Exception ignored) {
                // 만료 등 어떤 예외든 무시하고 성공 응답
            }
        }

        // 항상 성공 응답
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}