package com.govos.srh.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SearchRequest(
        @NotBlank
        String indexCode,
        @NotNull
        UUID organizationId,
        UUID userId,
        String queryText,
        SearchQueryMode queryMode,
        @Valid
        SearchFilters filters,
        @Valid
        SearchPage page,
        List<@Valid SearchSort> sort,
        Boolean highlight,
        List<String> facetFields
) {
}
