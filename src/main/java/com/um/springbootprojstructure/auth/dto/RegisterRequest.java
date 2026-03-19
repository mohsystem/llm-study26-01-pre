package com.um.springbootprojstructure.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @Size(max = 120) String displayName,
        @Size(max = 30) String phoneNumber,
        Boolean mfaEnabled
) {}

