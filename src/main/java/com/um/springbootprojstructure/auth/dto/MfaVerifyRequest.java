package com.um.springbootprojstructure.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MfaVerifyRequest(
        @NotBlank @Size(min = 16, max = 128) String preAuthToken,
        @NotBlank @Pattern(regexp = "^\\d{6}$") String passcode
) {}

