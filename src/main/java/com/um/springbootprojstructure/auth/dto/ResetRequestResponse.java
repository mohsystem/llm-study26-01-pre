package com.um.springbootprojstructure.auth.dto;

public record ResetRequestResponse(
        String status,
        String resetToken
) {}

