package com.govos.srh.ai;

import com.govos.srh.query.SearchFilters;
import com.govos.srh.query.SearchPage;
import com.govos.srh.query.SearchSort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SemanticSearchRequest(
        @NotBlank
        String indexCode,
        @NotNull
        UUID organizationId,
        UUID userId,
        @NotBlank
        String queryText,
        @Valid
        SearchFilters filters,
        @Valid
        SearchPage page,
        Integer topK
) {
}
