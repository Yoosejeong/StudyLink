package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.UserHandler;
import com.studycrew.studyBoard.config.CacheConfig;
import com.studycrew.studyBoard.config.props.PresignProps;
import com.studycrew.studyBoard.config.props.S3Props;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.studycrew.studyBoard.config.CacheConfig.*;
import static com.studycrew.studyBoard.dto.S3DTO.S3RequestDTO.*;
import static com.studycrew.studyBoard.dto.S3DTO.S3ResponseDTO.*;
import static com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO.*;

@Service
@Transactional
@RequiredArgsConstructor
public class S3Service {
    private final S3Presigner preSigner;
    private final S3Props props;
    private final PresignProps presign;
    private final UserRepository userRepository;
    private final com.github.benmanes.caffeine.cache.Cache<String, Entry> presignedGetCache;
    private static final Duration SKEW = Duration.ofSeconds(30);

    /**
     * 업로드용 Presigned PUT URL 발급
     * */
    public PresignPutResponse presignPut(Long userId, PresignPutRequest req) {
        var ttl = presign.putTtl();

        // 1) 키 결정
        String safeName = sanitizeFilename(req.getFilename());
        String prefix = "profiles/" + userId + "/";
        String key = prefix + UUID.randomUUID() + "_" + safeName;

        // 2) PutObjectRequest(서명에 포함될 헤더/속성)
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .contentType(req.getContentType())
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        // 3) Presign
        var presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(put)
                .build();

        var presigned = preSigner.presignPutObject(presignReq);
        URL url = presigned.url();

        // S3가 요구하는 서명 헤더: Map<String, List<String>> -> Map<String, String>
        Map<String, String> headers = presigned.signedHeaders().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.join(",", e.getValue()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // Content-Type은 PUT 시 presign과 동일해야 함
        headers.putIfAbsent("Content-Type", req.getContentType());
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));
        String oldKey = user.getProfileUrl();
        user.changeProfileUrl(key);

        if (oldKey != null) {
            presignedGetCache.invalidate(oldKey);
        }

        return PresignPutResponse.builder()
                .method("PUT")
                .url(url.toString())
                .headers(headers)
                .key(key)
                .expiresAt(Instant.now().plus(ttl))
                .build();
    }

    /**
     * 다운로드용 Presigned GET URL 발급
     **/
    public PresignGetResponse presignGet(String key) {
        var ttl = presign.getTtl();

        var get = GetObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .build();

        var presigned = preSigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(get)
                .build());

        return PresignGetResponse.builder()
                .url(presigned.url().toString())
                .expiresAt(Instant.now().plus(ttl))
                .build();
    }

    private String sanitizeFilename(@NotBlank String filename) {
        String base = filename.replace("\\", "/");
        base = base.substring(base.lastIndexOf('/') + 1);  // 경로 제거
        base = base.replaceAll("[\\r\\n]", "").trim();     // 개행 제거
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");    // 안전 문자만
        return base.isBlank() ? "file" : base;
    }

    @Transactional(readOnly = true)
    public PresignGetResponse presignGetCached(String key) {
        // 캐시에 없으면 생성
        Entry entry = presignedGetCache.get(key, k -> {
            var fresh = presignGet(k);
            return new Entry(fresh.getUrl(), fresh.getExpiresAt());
        });

        // 만료 임박일시 갱신
        if (entry.exp().isBefore(Instant.now().plus(SKEW))) {
            var fresh = presignGet(key);
            entry = new Entry(fresh.getUrl(), fresh.getExpiresAt());
            presignedGetCache.put(key, entry);
        }

        // 최종 반환
        return PresignGetResponse.builder()
                .url(entry.url())
                .expiresAt(entry.exp())
                .build();
    }

    /**
     * ✅ 여러 key를 한 번에 URL로 변환 (목록 화면 최적화)
     */
    @Transactional(readOnly = true)
    public List<PresignGetItemResponse> batchPresignGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) return List.of();

        // 중복 제거 + 널/빈 필터
        var distinct = keys.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        var items = new ArrayList<PresignGetItemResponse>(distinct.size());
        for (String k : distinct) {
            var p = presignGetCached(k);
            items.add(new PresignGetItemResponse(k, p.getUrl(), p.getExpiresAt()));
        }
        return items;
    }


    @Transactional(readOnly = true)
    public headerProfileDTO getMeHeader(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));
        String url = null; Instant exp = null;
        if (user.getProfileUrl() != null) {
            var p = presignGetCached(user.getProfileUrl());
            url = p.getUrl();
            exp = p.getExpiresAt();
        }
        return headerProfileDTO.builder()
                .profileUrl(url)
                .expiresAt(exp).build();
    }
}