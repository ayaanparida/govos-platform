package com.govos.srh.engine;

import java.util.List;
import java.util.Map;

public record SearchEngineQueryResult(
        long totalHits,
        List<Map<String, Object>> hits
) {
}
