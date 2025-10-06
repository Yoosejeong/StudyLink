package com.studycrew.studyBoard.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studycrew.studyBoard.apiPayload.ApiResponse;
import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.code.status.SuccessStatus;
import com.studycrew.studyBoard.dto.UserDTO.UserLoginRequestDTO;
import com.studycrew.studyBoard.service.RefreshService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final JWTUtil jwtUtil;
    private final RefreshService refreshService;
    private final ObjectMapper objectMapper;

    public LoginFilter(String loginUrl, AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshService refreshService, ObjectMapper objectMapper) {
        super(new AntPathRequestMatcher(loginUrl));
        setAuthenticationManager(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.refreshService = refreshService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserLoginRequestDTO userLoginRequestDTO = objectMapper.readValue(request.getInputStream(), UserLoginRequestDTO.class);

            String email = userLoginRequestDTO.email();
            String password = userLoginRequestDTO.password();

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
        refreshService.save(email, refresh, Duration.ofDays(1));

        response.setHeader("Authorization", "Bearer " + access);
        response.addCookie(createCookie("refresh", refresh));
        ApiResponse<Void> body = ApiResponse.of(SuccessStatus._USER_LOGIN_SUCCESS);

        writeResponse(response, body);
    }

    private void writeResponse(HttpServletResponse response,
                               ApiResponse<?> apiResponse) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        ApiResponse<Void> body = ApiResponse.onFailure(ErrorStatus._USER_LOGIN_INVALID_CREDENTIALS);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeResponse(response, body);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "LAX");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
