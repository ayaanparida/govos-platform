package com.govos.security.handler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SecurityErrorResponse(
        String code,
        String message,
        String path,
        Instant timestamp,
        String requestId,
        List<String> errors
) {
}
