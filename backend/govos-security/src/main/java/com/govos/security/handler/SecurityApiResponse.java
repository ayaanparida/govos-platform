package com.govos.security.handler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * API envelope aligned with {@code com.govos.api.common.response.ApiResponse} for security handlers.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SecurityApiResponse<T>(
        boolean success,
        T data,
        String message,
        Instant timestamp,
        String requestId
) {
}
