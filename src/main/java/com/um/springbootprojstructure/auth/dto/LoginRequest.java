package com.um.springbootprojstructure.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Size(max = 254) String login,
        @NotBlank @Size(min = 8, max = 128) String password
) {}

