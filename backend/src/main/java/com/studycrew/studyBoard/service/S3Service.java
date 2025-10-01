package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.UserHandler;
import com.studycrew.studyBoard.config.props.PresignProps;
import com.studycrew.studyBoard.config.props.S3Props;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

import static com.studycrew.studyBoard.dto.S3DTO.S3RequestDTO.*;
import static com.studycrew.studyBoard.dto.S3DTO.S3ResponseDTO.*;
import static com.studycrew.studyBoard.dto.UserDTO.UserResponseDTO.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final S3Presigner preSigner;
    private final S3Props props;
    private final PresignProps presign;
    private final S3Client s3;
    private final UserRepository userRepository;

    public String buildPublicUrl(String key) {
        if (key == null || key.isBlank()) return null;

        return "https://" + props.bucket()
                + ".s3." + props.region()
                + ".amazonaws.com/" + key;
    }

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

        return PresignPutResponse.builder()
                .method("PUT")
                .url(url.toString())
                .headers(headers)
                .key(key)
                .expiresAt(Instant.now().plus(ttl))
                .build();
    }

    @Transactional(readOnly = true)
    public headerProfileDTO getMeHeader(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        String url = buildPublicUrl(user.getProfileUrl());

        return headerProfileDTO.builder()
                .profileUrl(url)
                .build();
    }

    @Transactional
    public void confirmProfile(Long userId, String newKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        String oldKey = user.getProfileUrl();
        if (Objects.equals(oldKey, newKey)) return;

        user.changeProfileUrl(newKey);

        if (oldKey != null) {
            try {
                s3.deleteObject(DeleteObjectRequest.builder()
                        .bucket(props.bucket())
                        .key(oldKey)
                        .build());
            } catch (Exception e) {
                log.warn("[S3] failed to delete old profile key={} : {}", oldKey, e.getMessage());
            }
        }
    }

    private String sanitizeFilename(@NotBlank String filename) {
        String base = filename.replace("\\", "/");
        base = base.substring(base.lastIndexOf('/') + 1);  // 경로 제거
        base = base.replaceAll("[\\r\\n]", "").trim();     // 개행 제거
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");    // 안전 문자만
        return base.isBlank() ? "file" : base;
    }

}