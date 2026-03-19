package com.um.springbootprojstructure.auth.service;

import com.um.springbootprojstructure.auth.domain.ApiKey;
import com.um.springbootprojstructure.auth.dto.ApiKeyItemResponse;
import com.um.springbootprojstructure.auth.dto.CreateApiKeyRequest;
import com.um.springbootprojstructure.auth.dto.CreateApiKeyResponse;
import com.um.springbootprojstructure.auth.dto.StatusResponse;
import com.um.springbootprojstructure.auth.repository.ApiKeyRepository;
import com.um.springbootprojstructure.common.exception.ApiException;
import com.um.springbootprojstructure.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiKeyServiceImpl implements ApiKeyService {
    private static final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final ApiKeyRepository apiKeyRepository;

    @Override
    public CreateApiKeyResponse issue(UUID authenticatedUserId, CreateApiKeyRequest request) {
        var user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "user not found"));

        Instant expiresAt = request.expiresAt();
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            throw ApiException.badRequest("API_KEY_EXPIRES_AT_INVALID", "expiresAt must be in the future");
        }

        var rawKey = generateRawKey();
        var prefix = rawKey.substring(0, Math.min(12, rawKey.length()));

        var entity = new ApiKey();
        entity.setUser(user);
        entity.setKeyPrefix(prefix);
        entity.setKeyHashSha256(sha256Hex(rawKey));
        entity.setName(StringUtils.hasText(request.name()) ? request.name().trim() : null);
        entity.setExpiresAt(expiresAt);

        entity = apiKeyRepository.save(entity);

        return new CreateApiKeyResponse(entity.getId(), rawKey, "ISSUED", entity.getKeyPrefix(), entity.getExpiresAt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyItemResponse> list(UUID authenticatedUserId) {
        var keys = apiKeyRepository.findAllByUserId(authenticatedUserId);
        var now = Instant.now();
        return keys.stream().map(k -> toItem(k, now)).toList();
    }

    @Override
    public StatusResponse revoke(UUID authenticatedUserId, UUID keyId) {
        var key = apiKeyRepository.findByIdAndUserId(keyId, authenticatedUserId)
                .orElseThrow(() -> ApiException.notFound("API_KEY_NOT_FOUND", "api key not found"));

        if (key.getRevokedAt() == null) {
            key.setRevokedAt(Instant.now());
            apiKeyRepository.save(key);
        }

        return new StatusResponse("API_KEY_REVOKED");
    }

    private static ApiKeyItemResponse toItem(ApiKey k, Instant now) {
        return new ApiKeyItemResponse(
                k.getId(),
                k.getName(),
                k.getKeyPrefix(),
                statusOf(k, now),
                k.getCreatedAt(),
                k.getExpiresAt(),
                k.getRevokedAt()
        );
    }

    private static String statusOf(ApiKey k, Instant now) {
        if (k.getRevokedAt() != null) return "REVOKED";
        if (k.getExpiresAt() != null && !k.getExpiresAt().isAfter(now)) return "EXPIRED";
        return "ACTIVE";
    }

    private static String generateRawKey() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "ak_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            var digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("sha-256 not available", e);
        }
    }
}

