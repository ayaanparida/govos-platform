package com.govos.srh.query;

import java.util.List;
import java.util.Map;

public record SearchResult(
        String id,
        Double score,
        Map<String, Object> source,
        Map<String, List<String>> highlights
) {
}
