package com.um.springbootprojstructure.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetRequestRequest(
        @NotBlank @Size(max = 254) String login
) {}

