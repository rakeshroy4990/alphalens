package com.alphalens.dto.response;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        List<String> details,
        Instant timestamp
) {
    public static ApiErrorResponse of(int status, String error, String message, List<String> details) {
        return new ApiErrorResponse(status, error, message, details, Instant.now());
    }
}
