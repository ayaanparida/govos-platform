package com.govos.api.common.response;

public record ValidationError(
        String field,
        String message,
        Object rejectedValue
) {
}
