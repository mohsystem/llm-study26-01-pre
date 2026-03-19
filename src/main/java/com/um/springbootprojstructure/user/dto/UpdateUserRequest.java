package com.um.springbootprojstructure.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, max = 50) String username,
        @Email @Size(max = 254) String email,
        @Size(max = 120) String displayName,
        Boolean active
) {}

