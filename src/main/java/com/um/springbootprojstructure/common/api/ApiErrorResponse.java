package com.um.springbootprojstructure.common.api;

import java.util.Map;

public record ApiErrorResponse(
        String status,
        String code,
        String message,
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponse rejected(String code, String message) {
        return new ApiErrorResponse("REJECTED", code, message, null);
    }

    public static ApiErrorResponse rejected(String code, String message, Map<String, String> fieldErrors) {
        return new ApiErrorResponse("REJECTED", code, message, fieldErrors);
    }
}

