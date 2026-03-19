package com.um.springbootprojstructure.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateApiKeyResponse(
        UUID keyId,
        String apiKey,
        String status,
        String keyPrefix,
        Instant expiresAt
) {}

