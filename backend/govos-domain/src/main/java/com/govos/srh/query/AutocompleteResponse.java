package com.govos.srh.query;

import java.util.List;

public record AutocompleteResponse(
        List<String> suggestions,
        long executionTimeMs
) {
}
