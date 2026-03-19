package com.um.springbootprojstructure.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetConfirmRequest(
        @NotBlank @Size(min = 16, max = 128) String resetToken,
        @NotBlank @Size(min = 8, max = 128) String newPassword
) {}

