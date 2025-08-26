package com.studycrew.studyBoard.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class CacheConfig {

    // 캐시에 들어갈 값 타입 설정 (url : presigned Get URL, exp: presigned URL 만료 시각
    public record Entry(String url, Instant exp) {}

    // 만료 30초 전에 무효화
    private static final long SKEW_MILLIS = 30_000;

    @Bean
    public Cache<String, Entry> presignedGetCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000) // 메모리에 최대로 보관할 key 수
                .expireAfter(new Expiry<String, Entry>() { // 엔트리하다 만료 시간 계산하는 로직
                    // 처음에 만료까지 남은 시간을 반환
                    @Override
                    public long expireAfterCreate(String key, Entry value, long currentTime) {
                        long ns = ttlNanos(value);
                        return ns;
                    }
                    // 같은 key로 값이 갱신될 때 다시 계산
                    @Override
                    public long expireAfterUpdate(String key, Entry value, long currentDuration, long now) {
                        long ns = ttlNanos(value); //  갱신 시 새 exp 기준 재계산
                        return ns;
                    }
                    // 읽기 시에는 바꾸지 않음.
                    @Override
                    public long expireAfterRead(String key, Entry value, long currentDuration, long now) {
                        return currentDuration;
                    }
                })
                .recordStats() // 히트율/미스 통계 수집 가능
                .build();
    }

    private static long toMs(long ns) { return TimeUnit.NANOSECONDS.toMillis(ns); }

    //현재 시각(nowMs)와 presign 만료(expMs)로부터 남은 시간 계산 후 SKEW(30초) 만큼 빼서 리턴
    private long ttlNanos(Entry v) {
        long nowMs = System.currentTimeMillis();
        long expMs = v.exp().toEpochMilli();
        long ttlMs = Math.max(1, expMs - nowMs - SKEW_MILLIS);
        return TimeUnit.MILLISECONDS.toNanos(ttlMs);
    }

}
