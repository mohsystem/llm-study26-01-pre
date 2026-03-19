package com.um.springbootprojstructure.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MfaChallengeRequest(
        @NotBlank @Size(min = 16, max = 128) String preAuthToken
) {}

