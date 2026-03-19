package com.um.springbootprojstructure.auth.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID accountId,
        String status
) {}

