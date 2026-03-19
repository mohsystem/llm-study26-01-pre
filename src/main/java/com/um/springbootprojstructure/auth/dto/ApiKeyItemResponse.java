package com.um.springbootprojstructure.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyItemResponse(
        UUID keyId,
        String name,
        String keyPrefix,
        String status,
        Instant createdAt,
        Instant expiresAt,
        Instant revokedAt
) {}

