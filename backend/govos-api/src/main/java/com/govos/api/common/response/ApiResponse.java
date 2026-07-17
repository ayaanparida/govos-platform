package com.govos.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        Instant timestamp,
        String requestId
) {

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ok(data, message, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message, String requestId) {
        return new ApiResponse<>(true, data, message, Instant.now(), requestId);
    }
}
