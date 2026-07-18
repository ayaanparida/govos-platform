package com.govos.srh.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AutocompleteRequest(
        @NotBlank
        String indexCode,
        @NotNull
        UUID organizationId,
        UUID userId,
        @NotBlank
        String prefix,
        String entityType,
        Integer limit
) {
    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 10;
}
