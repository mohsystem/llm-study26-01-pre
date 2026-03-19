package com.um.springbootprojstructure.auth.dto;

import java.time.Instant;

public record MfaVerifyResponse(
        String status,
        String token,
        Instant expiresAt
) {}

