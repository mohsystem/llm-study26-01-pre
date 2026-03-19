package com.um.springbootprojstructure.auth.dto;

import java.time.Instant;

public record LoginResponse(
        String status,
        String token,
        Instant expiresAt,
        String preAuthToken
) {}

