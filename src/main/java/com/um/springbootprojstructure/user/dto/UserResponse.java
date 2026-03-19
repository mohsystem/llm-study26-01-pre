package com.um.springbootprojstructure.user.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        String role,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}

