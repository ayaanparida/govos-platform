package com.govos.api.common.validation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Standard pagination query parameters for list endpoints.
 */
public record PaginationRequest(
        @Min(0)
        int page,
        @Min(1) @Max(100)
        int size,
        String sort
) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;

    public PaginationRequest {
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
    }

    public static PaginationRequest defaults() {
        return new PaginationRequest(DEFAULT_PAGE, DEFAULT_SIZE, null);
    }
}
