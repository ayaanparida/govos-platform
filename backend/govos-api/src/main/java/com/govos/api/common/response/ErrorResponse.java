package com.govos.api.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        String message,
        String path,
        Instant timestamp,
        String requestId,
        List<ValidationError> errors
) {

    public static ErrorResponse of(
            String code,
            String message,
            String path,
            String requestId) {
        return new ErrorResponse(code, message, path, Instant.now(), requestId, null);
    }

    public static ErrorResponse of(
            String code,
            String message,
            String path,
            String requestId,
            List<ValidationError> errors) {
        return new ErrorResponse(code, message, path, Instant.now(), requestId, errors);
    }
}
