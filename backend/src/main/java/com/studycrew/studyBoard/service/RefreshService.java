package com.studycrew.studyBoard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final StringRedisTemplate redis;

    private String key(String email){
        return "refresh:u:" + email;
    }

    /** 로그인/회원가입 완료 시 저장 */
    public void save(String email, String refreshToken, Duration ttl) {
        redis.opsForValue().set(key(email), refreshToken, ttl);
    }

    /** 재발급 시, 요청 토큰이 현재 저장된 토큰과 같은지 확인 */
    public boolean isValid(String email, String refreshToken) {
        String stored = redis.opsForValue().get(key(email));
        return refreshToken != null && refreshToken.equals(stored);
    }

    /** 로테이트(새 토큰으로 교체 + TTL 갱신) */
    public void rotate(String email, String newRefreshToken, Duration ttl) {
        redis.opsForValue().set(key(email), newRefreshToken, ttl);
    }

    /** 로그아웃/전면 폐기 */
    public void revoke(String email) {
        redis.delete(key(email));
    }
}
