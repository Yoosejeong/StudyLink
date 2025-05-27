package com.studycrew.studyBoard.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycrew.studyBoard.dto.UserDTO.UserLoginRequestDTO;
import com.studycrew.studyBoard.entity.Refresh;
import com.studycrew.studyBoard.repository.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public LoginFilter(String loginUrl, AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        super(new AntPathRequestMatcher(loginUrl));
        setAuthenticationManager(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserLoginRequestDTO userLoginRequestDTO = objectMapper.readValue(request.getInputStream(), UserLoginRequestDTO.class);

            String email = userLoginRequestDTO.getEmail();
            String password = userLoginRequestDTO.getPassword();

            log.info("로그인 요청 email={}, password={}", email, password);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, password);

            return getAuthenticationManager().authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException("요청 파싱 중 오류 발생", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) throws IOException {

        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        String access = jwtUtil.createJwt("access", email, role, 600000L);
        String refresh = jwtUtil.createJwt("refresh", email, role, 86400000L);

        //Refresh 토큰 저장
        addRefreshEntity(email, refresh, 86400000L);

        response.setHeader("Authorization", "Bearer " + access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpServletResponse.SC_OK);
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

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }
}
