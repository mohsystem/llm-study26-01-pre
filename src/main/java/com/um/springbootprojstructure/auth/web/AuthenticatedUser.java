package com.um.springbootprojstructure.auth.web;

import com.um.springbootprojstructure.user.domain.UserRole;
import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String username,
        UserRole role
) {}

