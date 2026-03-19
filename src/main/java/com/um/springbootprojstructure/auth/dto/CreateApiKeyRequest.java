package com.um.springbootprojstructure.auth.dto;

import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateApiKeyRequest(
        @Size(max = 100) String name,
        Instant expiresAt
) {}

