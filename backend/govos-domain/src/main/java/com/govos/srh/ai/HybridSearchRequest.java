package com.govos.srh.ai;

import com.govos.srh.query.SearchFilters;
import com.govos.srh.query.SearchPage;
import com.govos.srh.query.SearchQueryMode;
import com.govos.srh.query.SearchSort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record HybridSearchRequest(
        @NotBlank
        String indexCode,
        @NotNull
        UUID organizationId,
        UUID userId,
        @NotBlank
        String queryText,
        SearchQueryMode queryMode,
        @Valid
        SearchFilters filters,
        @Valid
        SearchPage page,
        List<@Valid SearchSort> sort,
        Boolean highlight,
        List<String> facetFields,
        Integer topK,
        Double keywordWeight,
        Double semanticWeight
) {
}
